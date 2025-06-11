Plant-disease-detection

🌱 项目简介
本项目是一款基于 Android 平台的离线植物病害识别应用，通过叶片图像分析实现植物病害检测，内置轻量级机器学习模型，支持照片分析与实时相机分析，为农业生产和园艺管理提供智能诊断解决方案。

🚀 主要功能
1.离线识别
无需网络，田间地头随时用。
2.多种识别方式
相册导入叶片照片识别
实时相机拍摄识别
语音识别快速操作
3.病害知识库
内置多作物、多病害的详细知识库，支持中英文切换
4.识别历史记录
自动保存每次识别结果，便于追溯和管理
5.我的花园管理
支持自定义添加、编辑、管理植物，记录养护日志
6.详细病害详情页
展示病害症状、发病机制、防治建议等丰富信息

🖼️ 主要界面预览
1. 主界面（识别入口）
   支持拍照、相册、语音、知识库、历史、花园等快捷入口
   识别结果展示置信度与病害名称
   ![571a89079dfb23ac713009cfd62a5cca_720](https://github.com/user-attachments/assets/1eef97f2-c8af-4cc2-8de0-7eb2a3ea8593)


2. 病害知识库
   分类展示所有支持识别的作物及其病害
   支持搜索、筛选
   ![87718205e7510f5758cb14d706538b49](https://github.com/user-attachments/assets/7e9f2dad-cfc1-4365-83a5-6242619aea4f)


3. 病害详情页
   展示病害名称、致病因子、详细症状、防治建议等
   ![d480ee228bc823b3014969d56c3060a4](https://github.com/user-attachments/assets/d7c941fe-e4c5-441f-8f0b-a12e859b9ceb)


4. 我的花园
   管理自有植物，记录养护日志
   支持添加、编辑、删除植物
   ![81d53133e4139c18eb0e06dfb3cd26b0](https://github.com/user-attachments/assets/f455d4b7-1dc1-4119-8d55-361f12d7cf27)


5. 历史记录
   自动保存每次识别结果，支持回溯查看
   ![6e1820b722ddc55d7c246f47774e8fe1](https://github.com/user-attachments/assets/26225d5a-e9ac-4932-86dd-eae4520961bc)


🧠 技术架构
1. 机器学习模型
   算法：迁移学习 InceptionV3 卷积神经网络（CNN）
   部署：TensorFlow Lite，模型体积 < 15MB
   数据集：PlantVillage（54,305 张叶片图像，14 种作物，17 类病害+健康叶片）
   准确率：92.3%（测试集）

2. Android 应用
   语言：Kotlin + Java
   架构：MVVM（ViewModel/LiveData/Room）
   界面：Material Design，ConstraintLayout
   图像处理：OpenCV for Android
   依赖管理：Gradle

📦 目录结构
Plant-disease-detection/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/lazarus/aippa_theplantdoctorbeta/   # 主要业务代码
│   │       ├── res/layout/                                  # 各界面布局
│   │       ├── res/drawable/                                # 图片资源
│   │       ├── assets/                                      # 机器学习模型、病害知识库
│   │       └── AndroidManifest.xml
│   ├── build.gradle
│   └── README.md
├── ml/                                                      # 训练脚本与模型
├── build.gradle
└── ...

⚙️ 快速开始
1.环境准备
Android Studio 4.0+
Android 8.0+ 设备或模拟器
2.运行项目
克隆仓库
用 Android Studio 打开 app 目录
连接设备或启动模拟器，点击运行
3.模型与数据
机器学习模型已内置于 assets/plant_disease_model.tflite
病害知识库位于 assets/disease_description_zh.json（中文）和 assets/disease_description.json（英文）

📝 主要界面说明
主界面
顶部为叶片图片展示区
下方为识别按钮与结果显示
右下角悬浮按钮展开后可快捷进入各功能
![571a89079dfb23ac713009cfd62a5cca](https://github.com/user-attachments/assets/910e4216-c4d5-4cef-aa7b-e2f77a1bbeee)

病害知识库
按作物分类展示所有支持识别的病害
点击可查看详情
![d44515cb594ca05405f0e894e58b936b](https://github.com/user-attachments/assets/83d378e5-3e3b-4664-be7f-c0ecbcee36ac)

病害详情页
展示病害名称、致病因子、症状、防治建议等
支持从识别结果或知识库跳转
![db767b702fa521ccb8ec48b74fda9f23](https://github.com/user-attachments/assets/e23577cf-8341-4fd6-9dd7-84dba250246d)

我的花园
管理自有植物，记录养护日志
支持添加、编辑、删除植物
![5eca80abf487d691d52ff7a361af0178](https://github.com/user-attachments/assets/61253a68-395c-4767-9360-4013375784bc)

历史记录
自动保存每次识别结果，支持回溯查看
![51aca61dddc9f441fb21b90c470aeac0](https://github.com/user-attachments/assets/82cc0c48-e1e3-460c-828e-211064c1e331)

💡 特色亮点
离线识别，适应无网络环境
多语言支持（中英文自动切换）
轻量级模型，低端设备也能流畅运行
丰富的病害知识库，科学防治建议