# Annotated 2
Annotated is a development tool for Minecraft Forge mods.

It is an annotation processor that can generate networks and packets for you.

## Features

### Generating a network

Adding a channel annotation to a class will create a network associated with the class using the selected channel:  
* The ``@GenerateChannel`` annotation will generate a new channel with the given name and version.
* The ``@DelegateChannel`` annotation will use the channel defined using ``@GenerateChannel`` in the given class.

### Generating a packet

Annotated allows you to define very quickly and easily packets.  
To do so, first create in your network class the function you want to call on the target side.
Annotate it with either ``@ClientPacket`` if the target is the client side or ``@ServerPacket`` if it is the server side.  
A new packet class is generated and is automatically registered to the network.
The class contains a ``send`` method that takes in parameters a target and parameters and that will send the packet to the target. Once the packet is received on the target, the annotated function is called with the parameters passed to ``send``.
Client packets contain a ``sendTo`` method to send the packet to a single player and server packets contain ``sendToServer`` to send the packet to the server.

Some types in parameter of the annotated method have special meanings, won't be required in the ``send`` function and a value will automatically be assigned:  
* ``ServerPlayerEntity``: the sender of the packet
* ``NetworkEvent.Context``: the context of the packet

## Gradle setup

### With every IDE

* Add the JitPack repository in your build.gradle:

```groovy
repositories {
    maven {
        name = 'JitPack'
        url = 'https://jitpack.io'
    }
}
```

* Add Annotated to your dependencies:

```groovy
compileOnly 'com.github.LeBossMax2:Annotated:MC1.15.2-v2.0'
annotationProcessor 'com.github.LeBossMax2:Annotated:MC1.15.2-v2.0'
```

* Add the Minecraft libraries to the path for the annotation processor:

```groovy
configurations {
    annotationProcessor.extendsFrom minecraft
}
```

### With Eclipse

For eclipse, using the ``net.ltgt.apt-eclipse`` plugin is recommended to automatically configure eclipse:

```groovy
plugins {
    id 'net.ltgt.apt-eclipse' version '0.21'
}
```

The output path of the annotation processor should also be configured like so:

```groovy
eclipse {
	jdt.apt.genSrcDir = file('src/generated/java') // Set the path for the generated source code (optional)
	classpath.file.whenMerged {
		entries.findAll { it.kind == 'output' } *.path = 'bin/main' // Set the path for the generated classes to be accessible by Forge
	}
}
```

Then running ``./gradlew eclipseJdtApt eclipseFactorypath`` will configure eclipse to use the annotation processor.