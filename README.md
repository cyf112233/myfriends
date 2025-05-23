# MyFriends - 可爱的狐狸伙伴插件

一个为 Minecraft 服务器设计的可爱狐狸伙伴插件，让玩家可以与自己的狐狸伙伴互动、存储物品、传送等。

## ✨ 主要功能

- 🦊 **狐狸伙伴系统**
  - 可爱的狐狸伙伴会跟随玩家
  - 支持狐狸的存储空间功能
  - 可以随时将狐狸传送到身边
  - 支持狐狸的可见性设置
  - 支持红石相关设置

- 📦 **存储系统**
  - 每个玩家都有专属的狐狸存储空间
  - 数据自动保存，确保安全
  - 支持在线/离线数据管理

- ⚙️ **个性化设置**
  - 可自定义狐狸的可见性
  - 可调整红石相关设置
  - 支持多语言系统

- 🔌 **插件集成**
  - 支持 PlaceholderAPI 变量
  - 与服务器其他插件完美兼容

## 📋 命令列表

| 命令 | 描述 | 权限节点 |
|------|------|----------|
| `/fox` | 显示帮助信息 | `myfriends.fox` |
| `/fox storage` | 打开狐狸的存储空间 | `myfriends.fox.storage` |
| `/fox teleport` | 将狐狸传送到你身边 | `myfriends.fox.teleport` |
| `/fox settings` | 显示设置帮助 | `myfriends.fox.settings` |
| `/fox settings visibility` | 修改可见性设置 | `myfriends.fox.settings.visibility` |
| `/fox settings redstone` | 修改红石设置 | `myfriends.fox.settings.redstone` |

## 🔒 权限系统

所有权限默认对所有玩家开放，管理员可以根据需要调整：

- `myfriends.fox` - 基础权限
- `myfriends.fox.storage` - 存储空间权限
- `myfriends.fox.teleport` - 传送权限
- `myfriends.fox.settings` - 设置权限
- `myfriends.fox.settings.visibility` - 可见性设置权限
- `myfriends.fox.settings.redstone` - 红石设置权限

## 📦 安装要求

- Minecraft 服务器版本：1.13 或更高
- 必需插件：
  - Spigot/Paper 服务器
- 可选插件：
  - PlaceholderAPI（用于变量支持）

## 🛠️ 技术细节

- 使用 Java 8 开发
- 基于 Gradle 构建系统
- 主要依赖：
  - Spigot API
  - PlaceholderAPI（可选）
  - PluginBase 框架
  - JetBrains Annotations

## 🔄 数据管理

- 玩家设置数据会在玩家加入时自动加载
- 玩家退出时自动保存所有数据
- 狐狸存储数据实时保存
- 插件重启时自动清理所有狐狸实体
- 支持在线/离线数据管理

## 🎨 特色

- 可爱的狐狸伙伴设计
- 完整的数据保存系统
- 多语言支持
- 可自定义的设置系统
- 与服务器其他插件完美兼容
- 支持 PlaceholderAPI 变量

## 👥 作者信息

- 作者：cxkcxkckx
- 插件名称：MyFriends
- 版本：支持自动更新

## 📝 更新日志

### 最新版本
- 初始版本发布
- 实现基础狐狸伙伴系统
- 添加存储功能
- 添加传送功能
- 添加设置系统
- 支持 PlaceholderAPI

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来帮助改进这个插件！

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。 