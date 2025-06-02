# Campus Connect IM - 校园即时通讯系统

Campus Connect IM 是一个校园应用的附属即时通讯系统，作为校园信息化平台的重要组成部分，旨在为校园用户提供安全、高效、便捷的通讯解决方案。本系统需要与主系统进行集成，用户认证和基础信息均来自主系统。

## 项目截图

![image](https://github.com/user-attachments/assets/1120b148-d3b0-4529-a0d3-ef9b37b6b303)

![image](https://github.com/user-attachments/assets/4bf50617-2380-4af4-b97f-54dd44ca3d7f)

![image](https://github.com/user-attachments/assets/7b650e94-7a35-4bfc-b6bf-c5d25e8fb222)


## 🌟 功能特性

- 🎯 **特色功能**
  - ✅ 邮箱验证
  - ✅ 找回密码
  - ✅ AI广告

- 📱 **即时通讯**
  - ✅ 私聊功能
  - ✅ 群聊功能
  - ✅ 消息实时推送
  - ⏳ 支持文本、图片消息（语音、视频等多媒体消息待实现）
  - ⏳ 消息撤回功能（计划中）
  
- 👥 **用户管理**
  - ✅ JWT 认证（与主系统集成）
  - ✅ 用户信息管理（基础信息来自主系统）
  - ⏳ 在线状态显示
  
- 👬 **社交功能**
  - ✅ 好友管理
  - ⏳ 好友分组（计划中）
  - ⏳ 好友申请处理
  
- 👥 **群组功能**
  - ✅ 群组创建与管理
  - ⏳ 群成员管理
  - ⏳ 群公告（计划中）
  - ⏳ 群消息管理

> 注：
> - ✅ 表示已实现的功能
> - ⏳ 表示计划中/待实现的功能

## 🔗 系统依赖

本系统作为校园信息化平台的附属应用，需要：
1. 与主系统的用户中心集成，用于用户认证
2. 访问主系统的用户基础信息
3. 使用主系统的统一认证接口

## 🛠️ 技术栈

### 后端
- Spring Boot
- MySQL
- Redis
- WebSocket (STOMP)
- JWT 认证
- MyBatis
- Druid 连接池
- RestTemplate
- Lombok
- Hutool 工具包
- FastJson2
- 阿里云邮件服务

### 前端
- Vue.js 3 + Vite
- Element Plus
- WebSocket
- Axios
- Pinia 状态管理
- Vue Router
- IndexedDB
- Capacitor

### 部署技术
- Docker + Docker Compose
- Nginx

### 开发工具
- Maven
- Git

## 🚀 快速开始

### 环境要求
- JDK 17+
- Node.js 16+
- Docker & Docker Compose
- Maven 3.6+

### 使用Docker部署
1. 克隆项目
```bash
git clone https://github.com/xiaohan2004/campus-connect-IM
cd campus-connect-im
```

2. 打包前后端
```bash
# 打包后端
cd backend
mvn clean package -DskipTests
mv target/im-0.0.1-SNAPSHOT.jar ../docker/backend/

# 打包前端
cd ../frontend
npm install
npm run build
cp -r dist ../docker/frontend/
```

3. 配置环境变量
```bash
cp docker/.env.example docker/.env
# 编辑 .env 文件，设置必要的环境变量
```

4. 构建并启动服务
```bash
cd ../docker
docker-compose up -d
```

5. 访问服务
- 前端：http://localhost
- 后端：http://localhost:8080

## 📚 API 文档

详细的API文档请参考 [API.md](API.md)

## 💾 数据库设计

数据库设计文档请参考 [database_design.md](database_design.md)

## 📋 应用规划

应用规划文档请参考 [ApplicationPlan.md](ApplicationPlan.md)
