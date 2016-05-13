# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\QA\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
-dontwarn org.taptwo.android.widget.ViewFlow.**
-dontwarn com.hudomju.swipe.**
-dontwarn com.android.application.**
-dontwarn me.brendanweinstein.**
-dontwarn com.jeremyfeinstein.slidingmenu.**
-dontwarn com.mixpanel.android.**
-dontwarn com.zendesk.**
-dontwarn com.google.gson.Gson
-dontwarn okio.**
-dontwarn butterknife.internal.**
-dontwarn retrofit2.**
-dontwarn retrofit.**
-dontwarn org.webrtc.**
-dontwarn rx.**
-dontwarn com.opentok.android.**
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-dontwarn cz.msebera.httpclient.android.**
-dontwarn com.wdullaer.materialdatetimepicker.**
-optimizations !class/unboxing/enum