# Annotated
Annotated is at the same time a development tool and a library for MinecraftForge.

The tool includes an annotation processor that can generate code for you.

## Features :

* Generate packet class :

Annotated can generate a packet class from a "data" class (a class with fields for the data)
You can implement the ``IClientPacket`` interface to define what happens when the packet is received on the client and / or ``IServeurPacket`` for the server.
Then just add the ``@GeneratePacket`` annotation to your class and Annotated will automatically generate a packet class with the correct constructor, the fromBytes and the toByte methods.

* Generate a network class and register the packets :

Adding the ``@GenerateNetwork`` on your main class will generate a network with all your generated packets already registered.
You can init and access the generated network using ``public static final SimpleNetworkWrapper MOD_CHANNEL = ModTestAnnotatedNetwork.initNetwork();``.

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
compile "annotated:Annotated-1.11:release:dev"
annotationProcessor "annotated:Annotated-1.11:release:dev"
```

* It is also recomanded to use the plugin ``net.ltgt.apt`` to automtically configure gradle :
```
plugins {

    id 'net.ltgt.apt' version '0.19'

}
```
You can also add ``net.ltgt.apt-eclipse`` or ``net.ltgt.apt-idea`` to configure your IDE.
