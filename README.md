This is an open-source library that can be freely used in Android applications to detect ad blocker.

# Project overview #

Many ad blockers exist on Android, this is a real problem for developers that rely on ad incomes.

This project proposes an open source library that can detect most of ad blockers.

Then developers can display a dialog to inform user that an ad blocker has been detected and to propose, for example, to buy an ad-free version of the application or to quit.

# Details #

Currently, the following methods are used to detect ad blockers:
  * Search for known ad blockers application package names
  * Resolve known ad server domains and check if it redirects to a local address (work for both DNS & hosts file modification)
  * Check in hosts file for known patterns (work for hosts file modification)

Detection methods will not give false positive but may not detect some ad blockers.

To be done:
  * Ad filtering proxy detection

This library will be improved regularly to ensure a maximum ad blocker detection rate.

# Usage #
## Setup ##
The easiest way to use this library is to check out the project source code from svn (in trunk/AdBlockersDetector, click on Source to get the full address) or to download a tarball (click on Download).

Once you have the project, import it in your eclipse workspace and reference it in your application project (right click > Properties > Android > Library > Add).

## Examples ##

### Simple example ###
```
public void checkAdBlocker()
{
    final AdBlockersDetector abd = new AdBlockersDetector(this);
    // Asynchronous detection in a background thread
    abd.detectAdBlockers(new AdBlockersDetector.Callback()
    {
        @Override
        public void onResult(boolean adBlockerFound, Info info)
        {
            if(adBlockerFound)
            {
                new DialogBuilder(MyActivity.this)
                    .setAdBlockerInfo(info)
                    .show();
            }
        }   
    });
}
```
In this example, a dialog with only a Quit button is displayed on ad blocker detection.

The method checkAdBlocker() should be called in an Handler, by a delayed message sent in onCreate().

### Adding a contact button ###
A contact button can be added to propose to the user to send a mail to the developer:
```
public void checkAdBlocker()
{
    final AdBlockersDetector abd = new AdBlockersDetector(this);
    // Asynchronous detection in a background thread
    abd.detectAdBlockers(new AdBlockersDetector.Callback()
    {
        @Override
        public void onResult(boolean adBlockerFound, Info info)
        {
            if(adBlockerFound)
            {
                new DialogBuilder(MyActivity.this)
                    .setAdBlockerInfo(info)
                    .setContactButton("mail@example.com")
                    .show();
            }
        }   
    });
}
```

### Adding a buy button ###
```
public class MyActivity
    extends Activity
    implements AdBlockersDetector.Callback, DialogInterface.OnClickListener
{
    public void checkAdBlocker()
    {
        final AdBlockersDetector abd = new AdBlockersDetector(this);
        // Asynchronous detection in a background thread
        abd.detectAdBlockers(this);
    }

    @Override
    public void onResult(boolean adBlockerFound, Info info)
    {
        if(adBlockerFound)
        {
            new DialogBuilder(this)
                .setAdBlockerInfo(info)
                .setBuyButton(this)
                .show();
        }
    }
    
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        if(which == DialogBuilder.BUTTON_BUY)
        {
            // Propose to buy the ad-free version
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.android.example"));
            startActivity(intent);
            Tools.quitApplication();
            // It can be replaced by an in-app product...
        }
    }
}
```

# Improvements #

To improve the detector, it is possible to easily edit constant arrays in AdBlockersDetector.java.

Currently there are four of them.

## Adding app name ##
If you discover a new ad blocker application, just add its package name to the following array:
```
private static final String[] BLOCKERS_APP_NAMES = 
    {
    //...
    "full.application.package"
    };
```


## Blocked hosts ##
To check if some other ad servers may be blocked, complete the following array:
```
private static final String[] BLOCKED_HOSTS = 
    {
    //...
    "a.admob.com",
    "mm.admob.com"
    };
```
For each domain, the library check whether it resolves to a local address.

## hosts file path ##
If on some system the hosts file is in a special folder, add the full path to the following array:
```
private static final String[] HOSTS_FILES = 
    {
    "/etc/hosts",
    "/system/etc/hosts",
    "/data/data/hosts"
    };
```
The library checks if these files contain one of the pattern defined below.

## hosts file patterns ##
You can add patterns to look for in hosts file in the following array:
```
private static final String[] HOSTS_FILE_PATTERNS = 
    {
    "admob"
    };
```

## Adding translation ##
Currently, text strings (used in the provided dialog for example) are translated in English & French.
You can add support for your native language by adding a strings.xml file in res/values-xx.
