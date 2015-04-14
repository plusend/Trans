1. 在实际产品中使用时，需要将libs\AnyShare-SDK.jar，及armeabi及x86两个子目录（连同下面的so库)拷贝到apk工程的libs目录下。
   其中so库为声音解码native库。
   
2. SDK库需要以下权限，请确保在APK中声明：
    <uses-permission android:name="android.permission.READ_LOGS" />
    <uses-permission android:name="android.permission.GET_TASKS" />
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.WRITE_CONTACTS" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.RESTART_PACKAGES" />
	
3. SDK分为两层，建立物理网络连接 -> 建立TCP/IP连接。
   ShareManager基于TCP/IP建立分享通道，即上层必须建立好物理网络连接后再调用ShareManager。
   ShareWrapper包装了ShareManager，同时包含了“茄子快传”现有建立物理网络的功能: 
   一个设备建立个人热点(Host端)，其他设备扫描WLAN网络加入(Client端)，同时可选利用声音自动选择Host端（周围可能存在多个Host时)。
   
4. 在代码中使用开发工具包：
    a.创建ShareWrapper实例: 
        ShareWrapper.createInstance(context, nickname, icon, true);
        ShareWrapper api = ShareWrapper.getInstance();
        
        context使用application context;
        nickname和icon表示用户的昵称和图标索引，图标索引支持的范围[0,9);
        最后一个参数表示是否在启动AP后自动关闭数据网络。
        
    b.打开AP:
        注册网络状态监听器，接收网络状态的变化
        api.addNetworkStatusListener(mApHostListener);
        
        注册用户监听器，接收用户上下线的消息
        api.addUserListener(mUserListener);
        
        启动host，等待其他用户接入
        api.startHost(true);
        boolean参数表示是否广播声音，使client端收到声音后自动连接
        
    c.扫描热点并加入网络:
        注册网络状态监听器，接收网络状态的变化
        api.addNetworkStatusListener(mApClientListener);
        
        注册用户监听器，接收用户上下线的消息
        api.addUserListener(mUserListener);
        
        启动client，扫描所有由SDK创建的热点
        api.startClient(true);
        boolean参数表示是否监听声音，使client端收到声音后自动连接
        
        连接指定的热点
        api.connect(ssid)
        
    d.发送数据
        创建数据，参数含义如下：
        ContentType         Key
        App                 package name
        Music,Video,Image   media id
        Contact             contact id
        File                file path
        List<ItemShareRecord> items = api.createShareRecords(List<Pair<ContentType, String>> typeKeyPair);
        发送数据
        api.sendItems(items);
        
SDK的使用用例可以参考SDKTest工程
        
   