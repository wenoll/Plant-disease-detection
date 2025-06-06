# Plant-disease-detection
# Plant Disease Recognition App 📱🌿

一款基于Android的离线植物病害识别应用，通过叶片图像分析实现植物病害检测，内置机器学习模型，支持照片分析与实时相机分析，为农业生产提供智能诊断解决方案。


## 🚀 核心功能
1. **离线模式运行**  
   无需网络连接即可完成图像采集、处理与病害识别，适应田间地头无网络环境。
2. **双模式分析**  
   - **照片分析**：支持从相册导入叶片照片进行病害检测  
   - **实时相机分析**：调用手机摄像头实时拍摄并分析叶片状态  
3. **快速图像处理**  
   基于优化的机器学习模型，实现**毫秒级图像处理速度**，满足实时诊断需求。  
4. **详细结果输出**  
   - 病害类型识别（含置信度）  
   - 植物病害症状概述与发病机制说明  
   - 针对性病害管理建议（防治措施、用药推荐等）  
5. **轻量级友好界面**  
   采用Material Design设计规范，操作流程简洁直观，支持多语言切换（后续迭代）。


## 🧠 技术架构
### 模型实现
- **算法**：基于迁移学习的**InceptionV3**卷积神经网络（CNN）  
- **框架**：Keras + TensorFlow Lite（模型轻量化部署）  
- **训练数据**：  
  [PlantVillage公开数据集](https://www.plantvillage.org/)（54,305张叶片图像）  
  - 覆盖14种作物（苹果、番茄、土豆等）  
  - 包含17类病害（细菌性、真菌性、病毒性等）+ 健康叶片类别  
  - 数据划分：80%训练集 + 10%验证集 + 10%测试集  
- **模型性能**：  
  - 准确率：92.3%（测试集）  
  - 模型体积：< 15MB（TensorFlow Lite量化优化）

### 开发工具链
- **语言**：Kotlin + Java  
- **框架**：Android Jetpack（ViewModel/LiveData/WorkManager）  
- **图像处理**：OpenCV for Android（图像预处理）  
- **依赖管理**：Gradle + Maven  
- **开发环境**：Android Studio + GitHub Actions（CI/CD）


## 📊 数据集详情
### 数据规模
- 总样本量：54,305张图像  
- 作物种类：14种（苹果、樱桃、胡椒、大豆、草莓等）  
- 病害类型：  
  - 17种侵染性病害（如晚疫病、灰霉病、锈病等）  
  - 4种细菌性病害，2种卵菌病害，2种病毒性病害，1种螨虫病害  
  - 12种作物的健康叶片对照  

### 数据结构