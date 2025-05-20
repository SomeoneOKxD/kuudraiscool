import org.apache.commons.lang3.SystemUtils

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.kyori.blossom") version "1.3.1"
}

//Constants:

val baseGroup: String by project
val mcVersion: String by project
val version: String by project
val mixinGroup = "$baseGroup.mixin"
val modid: String by project
val transformerFile = file("src/main/resources/accesstransformer.cfg")

blossom {
    replaceToken("@VER@", version)
    replaceToken("@ID@", modid)
}

// Toolchains:
java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

// Minecraft configuration:
loom {
    log4jConfigs.from(file("log4j2.xml"))
    launchConfigs {
        "client" {
            property("mixin.debug", "true")
            arg("--tweakClass", "someoneok.kic.tweaker.KICModTweaker")
        }
    }
    runConfigs {
        "client" {
            if (SystemUtils.IS_OS_MAC_OSX) {
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        mixinConfig("mixins.$modid.json")
	    if (transformerFile.exists()) {
			println("Installing access transformer")
		    accessTransformer(transformerFile)
	    }
    }
    mixin {
        defaultRefmapName.set("mixins.$modid.refmap.json")
    }
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

// Dependencies:

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
    maven("https://repo.polyfrost.cc/releases/")
    maven("https://repo.hypixel.net/repository/Hypixel/")

    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1/")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

val modApiVersion = "1.0.1.1"

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    // If you don't want mixins, remove these lines
    shadowImpl("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
        isTransitive = false
    }
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")

    // If you don't want to log in with your real minecraft account, remove this line
    runtimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")

    compileOnly("cc.polyfrost:oneconfig-1.8.9-forge:0.2.2-alpha+")
    shadowImpl("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta17")

    compileOnly("net.hypixel:mod-api-forge:$modApiVersion")
    shadowImpl("net.hypixel:mod-api-forge-tweaker:$modApiVersion")

    compileOnly("com.squareup.okhttp3:okhttp:3.14.9")
    shadowImpl("com.squareup.okhttp3:okhttp:3.14.9")

    compileOnly("com.squareup.okio:okio:1.17.6")
    shadowImpl("com.squareup.okio:okio:1.17.6")
}

// Tasks:

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(org.gradle.jvm.tasks.Jar::class) {
    archiveBaseName.set(modid)
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
        this["MixinConfigs"] = "mixins.$modid.json"
	    if (transformerFile.exists())
			this["FMLAT"] = "${modid}_at.cfg"
        this["ModSide"] = "CLIENT"
        this["TweakOrder"] = "0"
        this["ForceLoadAsMod"] = true
        this["TweakClass"] = "someoneok.kic.tweaker.KICModTweaker"
    }
}

tasks.processResources {
    inputs.property("version", version)
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)
    inputs.property("basePackage", baseGroup)

    filesMatching(listOf("mcmod.info", "mixins.$modid.json")) {
        expand(inputs.properties)
    }

    rename("accesstransformer.cfg", "META-INF/${modid}_at.cfg")
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

tasks.jar {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    archiveClassifier.set("non-obfuscated-with-deps")
    configurations = listOf(shadowImpl)

    fun relocate(name: String) = relocate(name, "$baseGroup.deps.$name")
    relocate("net.hypixel.modapi.tweaker", "$baseGroup.deps.hypixel.modapitweaker.HypixelModAPITweaker")
    relocate("okhttp3", "$baseGroup.deps.okhttp3")
    relocate("okio", "$baseGroup.deps.okio")

    doLast {
        configurations.forEach {
            println("Copying dependencies into mod: ${it.files}")
        }
    }
}

tasks.register<Copy>("copyDeps") {
    from(configurations.runtimeClasspath)
    into("libs")
}

tasks.assemble.get().dependsOn(tasks.remapJar)
