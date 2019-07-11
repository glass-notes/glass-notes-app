# glass-notes-app

## Demo

[![Glass Notes demo YouTube video](https://img.youtube.com/vi/X09_pJ8Hj90/0.jpg)](https://www.youtube.com/watch?v=X09_pJ8Hj90)

## Installation 

1. `git clone https://github.com/glass-notes/glass-notes-app.git`
2. Open in Android Studio
3. Add `Preferences.java` to the package/folder `io.p13i.glassnotes.user`
4. Populate `Preferences.java` with:

```java
package io.p13i.glassnotes.user;

import android.content.Context;

import io.p13i.glassnotes.datastores.GlassNotesDataStore;
import io.p13i.glassnotes.datastores.github.GlassNotesGitHubAPIClient;
import io.p13i.glassnotes.datastores.localdisk.LocalDiskGlassNotesDataStore;


public class Preferences {
    /**
     * How often to save notes to the specified data store, in milliseconds
     */
    public final static int SAVE_PERIOD_MS = 5000;  // every 5 seconds

    /**
     * Set as LocalDiskGlassNotesDataStore or GlassNotesGitHubAPIClient
     * @param context
     * @return
     */
    public static GlassNotesDataStore getUserPreferredDataStore(Context context) {
        return new LocalDiskGlassNotesDataStore(context);
//        return new GlassNotesGitHubAPIClient("<<<INSERT GITHUB TOKEN FOR GIST ACCESS HERE>>>");
    }
}


```
5. Deploy to your device
