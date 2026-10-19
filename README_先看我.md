# 翻时 · 专注：无需 Android Studio，Mac 上云端编译 APK

## 用 GitHub 网页生成 APK

1. Mac 浏览器登录 https://github.com ，注册免费 GitHub 账号（如尚无）。
2. 右上角 **+ → New repository**，名称填 `FlipFocus`，选择 **Public**（公开仓库标准 Actions 通常免费），**勾选 Add a README file**，点击 **Create repository**。如选 Private，需注意 Actions 免费额度和计费限制。
3. 在 Finder 解压此 ZIP。按 **Command+Shift+.** 显示隐藏文件夹（重要！必须看到 `.github`）。
4. 打开 `翻时专注_GitHub云端打包工程`，选中**里面的所有文件和文件夹**，包括 `.github`、`app`、`gradle`、`settings.gradle`、`build.gradle` 等。注意不要把外层工程文件夹作为一级目录上传。
5. GitHub 仓库首页 → **Add file → Upload files**，将刚选中的所有内容拖入页面 → **Commit changes**。请确认网页上传后的根目录可直接看到 `app`、`build.gradle`、`settings.gradle`，同时 `.github/workflows/build-apk.yml` 可见。
6. 点击仓库的 **Actions**。进入 **编译翻时专注 APK** 工作流。如果没有自动运行，选择 **Run workflow** 手动运行。若提示同意启用 Actions，请先启用。
7. 工作流显示绿色成功后，点击该次运行，在底部 **Artifacts** 下载 **翻时专注-Xiaomi14-APK**（一般得到 ZIP，解压可见 `app-debug.apk`）。
8. 使用微信文件传输助手或 AirDrop 替代方式（如 QQ / 数据线 / 云盘）把 APK 从 Mac 传到小米14。手机文件管理器打开 APK，按系统提示授予该来源的安装权限后安装。

## 注意

- 本 ZIP 是可以提交到 GitHub Actions 编译的**源码工程**，不是 APK；本环境没有 Android SDK / 网络，因此未能现场实际编译验证。
- GitHub 公开仓库中的代码对所有人可见；本项目无个人账号、隐私数据和服务器密钥。
- 生成的是供个人安装的 **debug APK**，不需要 Play 商店和签名证书设置。**不要**把未知来源 APK 当作官方应用商店软件。
- 小米 HyperOS 对后台通知和非精确闹钟的限制可能令提醒略晚，安装后请先测试 1 分钟计时。
- 如果构建失败，点击红色工作流日志，复制报错即可定位修复。

## 源码功能



- 数字翻页外观与翻页过渡；24 小时本地时钟。
- 15 / 25 / 45 / 60 分钟预设，也可设置 **1–99 分钟**。
- 开始、暂停、继续、重置；进程停止后仍以保存的绝对截止时间计算剩余时间。
- 本地保存当天完成次数、累计完成分钟（没有联网，也不上传资料）。
- 专注/时钟界面可开启屏幕常亮；到时通过系统通知提醒（需授权）。
- 提醒声可在设置中关闭。

