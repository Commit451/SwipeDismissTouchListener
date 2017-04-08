# SwipeDismissTouchListener
Swipe dismiss on any ViewGroup

[![Build Status](https://travis-ci.org/Commit451/SwipeDismissTouchListener.svg?branch=master)](https://travis-ci.org/Commit451/SwipeDismissTouchListener) [![](https://jitpack.io/v/Commit451/SwipeDismissTouchListener.svg)](https://jitpack.io/#Commit451/SwipeDismissTouchListener)

# Usage
```java
View view = findViewById(R.id.card1);
SwipeDismissTouchListener swipeDismissTouchListener = new SwipeDismissTouchListener(view);
swipeDismissTouchListener.setOnDismissListener(new SwipeDismissTouchListener.OnDismissListener() {
    @Override
    public void onDismiss(@NonNull View view) {
        root.removeView(view);
    }
});
view.setOnTouchListener(swipeDismissTouchListener);
```
# Acknowledgements
Derived from the [sample](https://github.com/romannurik/Android-SwipeToDismiss) provided by Roman Nurik

License
--------

    Copyright 2013 Google
    Copyright 2017 Commit 451

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
