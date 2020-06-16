## MVideo     [![](https://jitpack.io/v/miaoyongjun/MVideo.svg)](https://jitpack.io/#miaoyongjun/MVideo)

已停止维护  可参考  https://github.com/MikaelZero/Mojito

An imitation of WeChat circle of friends video effects,  can drag and zoom ,video view,base on ijkplayer.


## Demo


![mvideo](https://github.com/miaoyongjun/MVideo/blob/master/1.gif?raw=true)

## Get started


#### **Step 1**  :  Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

```java
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

####  **Step 2**  :  Add the dependency

```java
dependencies {
	        compile 'com.github.miaoyongjun:MVideo:1.5'
	}
```

#### **Step 3**   :  register in mainifest.xml

```java
 <activity android:name="com.miaoyongjun.mdragvideo.DragVideoActivity"/>
```

#### **Step 4**  :  start

```java
MVideo.getInstance()
                .setPreviewImage("http://bmob-cdn-982.b0.upaiyun.com/2017/02/24/98754a6a401d5c48806b2b3863e32bed.jpg")
		.bind(new ImageLoaderAdapter() {
                                @Override
                                public void bind(ImageView imageView, String imagePath) {
                                    Glide.with(DisplayActivity.this).load(imagePath).into(imageView);
                                }
                            })
                .start(this,srcView,"http://bmob-cdn-982.b0.upaiyun.com/2017/02/23/266454624066f2b680707492a0664a97.mp4");
```

## About Cache


I think the cache needs to be handled by the developers themselves, so **MVideo** does not provide caching,I recommend a framework here . [AndroidVideoCache](https://github.com/danikula/AndroidVideoCache)

## Thanks


- [glide](https://github.com/bumptech/glide)
- [ijkplayer](https://github.com/Bilibili/ijkplayer)
- [DragPhotoView](https://github.com/githubwing/DragPhotoView)
