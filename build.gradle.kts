import net.minecraftforge.gradle.common.util.RunConfig
import java.text.SimpleDateFormat
import java.util.*

buildscript {
    repositories {
        maven { setUrl("https://files.minecraftforge.net/maven") }
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:3.+")
        classpath("org.ajoberstar.grgit:grgit-gradle:3.0.0-beta.1") // intellij for some reason doesn't recognize 3.1.1
    }
}
plugins {
    idea
    eclipse
    `maven-publish`
    signing
    id("com.github.johnrengelman.shadow").version("4.0.2")
}
apply(plugin = "net.minecraftforge.gradle")

val modid: String by project
val forgeVersion: String by project
val mappingsVersion: String by project

// final/rcX/betaX
val versionType: String by project
// set version string to arbitrary value (ie. patch releases from different branch)
val forceVersion: String by project
// snapshot or not snapshot?
val release: String by project

version = getModVersion()
group = "genesis" // http://maven.apache.org/guides/mini/guide-naming-conventions.html

base {
    archivesBaseName = modid
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    sourceSets["main"].apply {
        resources {
            srcDir("$rootDir/src/generated/resources")
        }
    }
}

idea {
    module.apply {
        inheritOutputDirs = true
    }
    module.isDownloadJavadoc = true
    module.isDownloadSources = true
}


minecraft {
    mappings("snapshot", mappingsVersion)

    // accessTransformer(file("$rootDir/src/main/resources/META-INF/accesstransformer.cfg"))

    fun setupConfig(conf: RunConfig) {
        conf.apply {
            property("forge.logging.markers", "") // or SCAN,REGISTRIES,REGISTRYDUMP
            property("forge.logging.console.level", "info") // or debug
            jvmArgs(listOf(
                    "-XX:-OmitStackTraceInFastThrow",
                    "-ea",
                    "-da:io.netty..."
            ))

            mods.create("genesis") {
                source(java.sourceSets["main"])
            }
        }
    }

    runs.create("client") {
        workingDirectory(project.file("run"))
        setupConfig(this)
    }
    runs.create("server") {
        workingDirectory(project.file("run"))
        setupConfig(this)
    }

    runs.create("data") {
        workingDirectory(project.file("run"))
        setupConfig(this)
        args("--mod", modid, "--all", "--output", file("src/generated/resources/"))
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:1.14.3-27.0.50")
}

tasks.getByName<Jar>("jar") {
    manifest {
        attributes(mapOf(
                "Specification-Title" to modid,
                "Specification-Vendor" to "Boethie",
                "Specification-Version" to "1",
                "Implementation-Title" to modid,
                "Implementation-Version" to version,
                "Implementation-Vendor" to "Boethie",
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()))
        )
    }
}

// Example configuration to allow publishing using the maven-publish task
// we define a custom artifact that is sourced from the reobfJar output task
// and then declare that to be published
// Note you'll need to add a repository here
val reobfFile = file("$buildDir/reobfJar/output.jar")
val reobfArtifact = artifacts.add("default", reobfFile) {
    type = "jar"
    builtBy("reobfJar")
}

publishing {
    publications.create("mavenJava", MavenPublication::class.java) {
        artifact(reobfArtifact)
    }
    repositories {
        maven {
            setUrl("file:///${project.projectDir}/mcmodsrepo")
        }
    }
}

fun getMcVersion(): String {
    return forgeVersion.split("-")[0]
}

//returns version string according to this: http://mcforge.readthedocs.org/en/latest/conventions/versioning/
//format: MCVERSION-MAJORMOD.MAJORAPI.MINOR.PATCH(-final/rcX/betaX)
//rcX and betaX are not implemented yet
fun getModVersion(): String {
    if (!forceVersion.isEmpty()) {
        return forceVersion
    }
    return getModVersion(false)
}

fun getModVersion(maven: Boolean): String {
    var version = "${getMcVersion()}-9999.9999.9999-unknown"
    try {
        val git = org.ajoberstar.grgit.Grgit.open()
        val describe = org.ajoberstar.grgit.operation.DescribeOp(git.repository).call()
        val branch = getGitBranch(git)
        val snapshotSuffix = if (release.toBoolean()) "" else "-SNAPSHOT"
        version = getModVersion(describe, branch, maven) + snapshotSuffix
    } catch (ex: Exception) {
        logger.error("Unknown error when accessing git repository! Are you sure the git repository exists?", ex)
    }
    println("Project version string: $version")
    return version
}

fun getGitBranch(git: org.ajoberstar.grgit.Grgit): String {
    var branch: String = git.branch.current().name
    if (branch == "HEAD") {
        branch = when {
            System.getenv("TRAVIS_BRANCH")?.isEmpty() == false -> // travis
                System.getenv("TRAVIS_BRANCH")
            System.getenv("GIT_BRANCH")?.isEmpty() == false -> // jenkins
                System.getenv("GIT_BRANCH")
            System.getenv("BRANCH_NAME")?.isEmpty() == false -> // ??? another jenkins alternative?
                System.getenv("BRANCH_NAME")
            else -> throw RuntimeException("Found HEAD branch! This is most likely caused by detached head state! Will assume unknown version!")
        }
    }

    if (branch.startsWith("origin/")) {
        branch = branch.substring("origin/".length)
    }
    return branch
}

fun getModVersion(describe: String, branch: String, mvn: Boolean): String {
    if (branch.startsWith("MC_")) {
        val branchMcVersion = branch.substring("MC_".length)
        if (branchMcVersion != getMcVersion()) {
            logger.warn("Branch version different than project MC version! MC version: " +
                    getMcVersion() + ", branch: " + branch + ", branch version: " + branchMcVersion)
        }
    }

    //branches "master" and "MC_something" are not appended to version sreing, everything else is
    //only builds from "master" and "MC_version" branches will actually use the correct versioning
    //but it allows to distinguish between builds from different branches even if version number is the same
    val branchSuffix = if (branch == "master" || branch.startsWith("MC_")) "" else ("-" + branch.replace("[^a-zA-Z0-9.-]", "_"))

    val baseVersionRegex = "v[0-9]+\\.[0-9]+"
    val unknownVersion = String.format("%s-UNKNOWN_VERSION%s%s", getMcVersion(), versionType, branchSuffix)
    if (!describe.contains('-')) {
        //is it the "vX.Y" format?
        if (describe.matches(Regex(baseVersionRegex))) {
            return if (mvn) String.format("%s-%s", getMcVersion(), describe)
            else String.format("%s-%s.0.0%s%s", getMcVersion(), describe, versionType, branchSuffix)
        }
        logger.error("Git describe information: \"$describe\" in unknown/incorrect format")
        return unknownVersion
    }
    //Describe format: vX.Y-build-hash
    val parts = describe.split("-")
    if (!parts[0].matches(Regex(baseVersionRegex))) {
        logger.error("Git describe information: \"$describe\" in unknown/incorrect format")
        return unknownVersion
    }
    if (!parts[1].matches(Regex("[0-9]+"))) {
        logger.error("Git describe information: \"$describe\" in unknown/incorrect format")
        return unknownVersion
    }
    val mcVersion = getMcVersion()
    val modAndApiVersion = parts[0].substring(1)
    //next we have commit-since-tag
    val commitSinceTag = Integer.parseInt(parts[1])

    val minor = commitSinceTag
    val patch = 0

    return if (mvn) String.format("%s-%s%s", mcVersion, modAndApiVersion, versionType)
    else String.format("%s-%s.%d.%d%s%s", mcVersion, modAndApiVersion, minor, patch, versionType, branchSuffix)
}