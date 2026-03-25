# 交投科技政企大模型 - 前端

## 技术栈
- Vue 3 + TypeScript + Vite 5
- Element Plus（UI组件库）
- Pinia（状态管理）
- Vue Router 4
- ECharts / vue-echarts（图表）
- WangEditor（富文本编辑器）
- Axios + SSE（HTTP/流式请求）

## 模块说明

| 模块 | 路径 | 功能 |
|------|------|------|
| 得心 | `/chat` | AI智能问答，支持文档上传，流式输出 |
| 应手 | `/write` | AI公文写作，法定/事务性公文，富文本编辑 |
| 问数 | `/data-query` | 自然语言问数，NL2SQL，图表可视化 |
| 知事 | `/agent` | 智能工具集，文件比对/校审/合规检查等 |

## 快速启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 开发启动（代理到后端 localhost:8080）
npm run dev

# 生产构建
npm run build
```

## 环境要求
- Node.js >= 18
- 后端服务运行在 localhost:8080

## 目录结构
```
src/
├── api/          # 接口封装（chat/write/dataQuery/agent/auth）
├── layout/       # 全局布局（TopNav + 四个模块侧边栏）
├── stores/       # Pinia状态管理
├── utils/        # 工具函数（SSE/request/export）
├── views/        # 页面组件
│   ├── Chat/     # 得心
│   ├── Write/    # 应手
│   ├── DataQuery/# 问数
│   ├── Agent/    # 知事
│   └── Login/    # 登录
├── types/        # TypeScript类型定义
└── styles/       # 全局样式（variables.css/global.css）
```

## 并发设计说明
- 每用户同时最多1个活跃SSE连接（切换时自动断开旧连接）
- SSE失败自动重连（指数退避，最多3次）
- 问数查询防抖处理（500ms）
- 大列表启用虚拟滚动（超50条历史记录）
- 各模块代码按需加载（Vite代码分割）
