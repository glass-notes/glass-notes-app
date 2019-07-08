# glass-notes-app

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
     * Get and set your oauth token here...
     */
    public final static String GITHUB_OAUTH_AUTH_HEADER = "Authorization: token <INSERT GITHUB TOKEN HERE>";

    /**
     * Set as LocalDiskGlassNotesDataStore or GlassNotesGitHubAPIClient
     * @param context
     * @return
     */
    public static GlassNotesDataStore getUserPreferredDataStore(Context context) {
        return new LocalDiskGlassNotesDataStore(context);
//        return new GlassNotesGitHubAPIClient();
    }
}
```
5. Deploy to your device
