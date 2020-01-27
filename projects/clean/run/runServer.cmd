@echo off

java -Xms2G -Xmx2G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M -cp ..\build\classes\java\main;C:\Minecraft\Forge\Libs1.15.1;C:\Minecraft\Forge\Libs1.15.1\client-extra.jar net.minecraft.server.MinecraftServer --bonusChest true
