# Annotated
Annotated is at the same time a development tool and a library for MinecraftForge.

The tool includes an annotation processor that can generate code for you.

## Features :

* Generate packet class :

Annotated can generate a packet class from a "data" class (a class with fields for the data)
You can implement the ``IClientPacket`` interface to define what happens when the packet is received on the client and / or ``IServerPacket`` for the server.
Then just add the ``@GeneratePacket`` annotation to your class and Annotated will automatically generate a packet class with the correct constructor, the fromBytes and the toByte methods.

* Generate a network class and register the packets :

Adding the ``@GenerateNetwork`` on your main class will generate a network with all your generated packets already registered.
You can init and access the generated network using ``public static final SimpleNetworkWrapper NETWORK = ModTestAnnotatedNetwork.initNetwork();``.

## Gradle setup :

* Add the CurseForge repository in out build.gradle :
```
repositories {
    maven {
        name = "CurseForge"
        url = "https://minecraft.curseforge.com/api/maven/"
    }
}
```

* Add Annotated to your dependencies :
```
compile "annotated:Annotated-1.12.2-1.0:1.0:dev"
annotationProcessor "annotated:Annotated-1.12.2-1.0:1.0:dev"
```

* It is also recomanded to use the plugin ``net.ltgt.apt`` to automtically configure gradle :
```
buildscript {
    repositories {
        jcenter()
		maven { url = "http://files.minecraftforge.net/maven" }
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        classpath 'net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT'
        classpath "net.ltgt.gradle:gradle-apt-plugin:0.21"
    }
}

apply plugin: 'net.ltgt.apt'
```
You can also add ``net.ltgt.apt-eclipse`` or ``net.ltgt.apt-idea`` to configure your IDE.
