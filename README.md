
# FileParsersToolbox

  Android library that allows you to parse and convert the contents of various file types into simple objects and data structures. This can help you, for instance, to conveniently show the contents of the files in the Android's native UI elements, e.g. TextViews, ImageViews, etc. It currently supports **.epub** and **.fb2** file types.
  
## Include in your project  
**Gradle dependency**  
  
Add this in your **app**-level **build.gradle** file:  
```groovy
dependencies {  
	...  
  
	def latest_version_tag = 1.0
	implementation "com.github.bogdandonduk:FileParsersToolbox:$latest_version_tag"  
  
	...  
}  
```  
You can always find the **latest_version_tag** [here](https://github.com/bogdandonduk/FileParsersToolbox/releases).  
  
Also make sure you have this repository in your **project**-level **build.gradle** file:  
```groovy  
allprojects {  
	repositories {  
		...  
  
		maven { url 'https://jitpack.io' }  
	}  
}  
```  

# Examples of usage
```kotlin 
// parse .epub files like this
val parsedBook = EpubUtils.parse(path = "storage/emulated/0/my_amazing_book.epub")

// and ep.fb2 files like this
val parsedAnotherBook = Fb2Utils.getValidFb2(path = "storage/emulated/0/my_another_amazing_book.fb2")

// there are also various utility methods to validate files, get their titles, cover images, etc
```
