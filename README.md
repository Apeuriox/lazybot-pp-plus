## lazybot-pp-plus
---
lazybot-pp-plus is a backend server for performance plus calculations, used in [lazybot](https://github.com/Apeuriox/lazybot-renewal), [kanon-bot](https://github.com/desu-life/Bot), and [yumu-bot](https://github.com/yumu-bot/yumu-bot). Original algorithm by [Syrin](https://github.com/Syriiin), ported to Rust version by [Zh_JK](https://github.com/fantasyzhjk), which you can found [here](https://github.com/fantasyzhjk/rosu-pp-ffi).
### Before exploring
---
#### Prerequisites
- Java 21
- A modern Relational Database
- Functional osu! client
#### For development
- Clone the project to your IDEs using `git clone https://github.com/fantasyzhjk/rosu-pp-ffi.git`
- Finish the application.yaml
- Run the script.sql to build the tables
- Run this command to install PerformancePlus calculation library into your project: `mvn install:install-file -Dfile=lib/RosuFFI-0.2.2.jar -DgroupId=me.zhjk -DartifactId=rosu-ppplus -Dversion=0.2.2 -Dpackaging=jar` and sync your maven settings
#### Optional Settings
- Environmental variable `LAZYBOT_DIR` was pointed to the folder which store the beatmap files (.osu format), name template: ${BeatmapID}.osu
- To add a rate limiter to your project, set `rate-limit.enabled` to true, and set your custom rate limit props
- To add an IP whitelist to your project, set `security.white-list` to true. and add your allowed IPs