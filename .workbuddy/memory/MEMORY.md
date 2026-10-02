# AirPlus-Fork 项目记忆

## 构建系统（重要约定）
- **构建后端 = Architectury Loom**（`gg.essential.loom` 0.10.0.+），**不是 ForgeGradle**。Kotlin DSL（`build.gradle.kts` / `settings.gradle.kts`）。
- 坐标：`mcVersion=1.8.9`、`modid=airplus`、`baseGroup=net.airplus`、`version=1.0`（均在 `gradle.properties`）。
- 产物流程：`shadowJar`（输出到 `build/intermediates`，classifier `non-obfuscated-with-deps`）→ `remapJar`（输出 `build/libs/airplus-1.0.jar`，已 remap）。`reobf` 已被 `remapJar` 取代。
- 运行 Gradle 需 **JDK 17+**（本机默认 Microsoft OpenJDK 21）；编译 target 经 foojay toolchain 取 **JDK 8**（因 nashorn 在 JDK15+ 移除，MC 1.8.9 必须 JDK8 启动）。
- **Mixin 由 Loom 注入**：`forge { mixinConfig("airplus.forge.mixins.json") }` + `mixin { defaultRefmapName }` + 启动参数 `--tweakClass org.spongepowered.asm.launch.MixinTweaker`。**非** CoreMod 自举。
- **两个自定义 ASM transformer 仍挂在 `MixinLoader`（`IFMLLoadingPlugin` CoreMod）**，仅作 transformer 载体（无 mixin 自举）：
  - `ForgeNetworkTransformer`：拦截 FML 握手包（关联 `ClientFixes.blockFML` exploit 模块）。
  - `AbstractJavaLinkerTransformer`：改写 JDK 内部类 `jdk.internal.dynalink.beans.AbstractJavaLinker`（脚本 nashorn/JS 引擎链接增强）；因目标为系统类加载的 JDK 内部类，**Mixin 无法替代**，必须 CoreMod。
- **AccessTransformer**：`src/main/resources/airplus_at.cfg`（SRG/searge 名），Loom 用 `forge { accessTransformer(file) }` 注入到 `META-INF/airplus_at.cfg`。
- Gradle wrapper：`distributionUrl` 指向 `gradle-8.10.2`；wrapper jar 需本地联网执行一次 `./gradlew wrapper` 更新。

## 已知待验证风险
- Loom forge 是否扫描第三方 `FMLCorePlugin` 未实测（沙箱无外网未构建）。若不加载 CoreMod：Fallback 是 `ForgeNetworkTransformer` 改 Mixin，`AbstractJavaLinkerTransformer` 改用 Loom forge 的 coremods JSON 机制或放弃 nashorn 增强。
