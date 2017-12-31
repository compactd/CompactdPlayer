# CompactdPlayer - Changelog

## 1.0.0-alpha.2 ( Sun Dec 31 2017 19:00:28 GMT+0100 (Paris, Madrid) )


## Bug Fixes
  - determine size using device metrics instead of default implementation
  ([380c4b34](https://github.com/compactd/CompactdPlayer/commit/380c4b34a52cf5eccfa5068ae304e303fd227f29))
  - use artwork ID instead of mediaCover as glide cache key
  ([648e3137](https://github.com/compactd/CompactdPlayer/commit/648e3137d90138972a050213bb3545ff50131f4b))
  - use relativelayout for grid_item to avoid text view pushing content
  ([811d1027](https://github.com/compactd/CompactdPlayer/commit/811d102776c09978368d2ad18db2e80660852eb9))
  - artist and album activity glitch due to singleInstance
  ([d8811ff6](https://github.com/compactd/CompactdPlayer/commit/d8811ff6336ffac5275d74861c0a61b3a5ec1c99))
  - musicservice not registered
  ([3b7f0262](https://github.com/compactd/CompactdPlayer/commit/3b7f0262dd62b7c3cb8df4716964b8317b8fccbe))
  - re-enabling sync wouldn't work
  ([4ca6a75f](https://github.com/compactd/CompactdPlayer/commit/4ca6a75f2c537c372a12ad6af71a5d0c52b2b64f))
  - eight times too large required space due to bytes vs bits
  ([51ea6253](https://github.com/compactd/CompactdPlayer/commit/51ea62535c57cdd28b1d64684b6569dc22474920))
  - album transition
  ([69ae586b](https://github.com/compactd/CompactdPlayer/commit/69ae586ba93be3bd35a9a287ac7e2895f79203c1))
  - mediaplayer progress called when not ready
  ([e03a121e](https://github.com/compactd/CompactdPlayer/commit/e03a121e0214e3cd55f6d2944bc8200ea6b77844))
  - on completion fired when changing datasource
  ([e2c18b36](https://github.com/compactd/CompactdPlayer/commit/e2c18b36a9405df2ea653da1f730607d21f47cfc))
  - player being destroyed when skipping track
  ([4ba91d4b](https://github.com/compactd/CompactdPlayer/commit/4ba91d4bf7dcc4251bd2a05dcbfc89e112651796))
  - player stopping when leaving artist or album activity
  ([d618eace](https://github.com/compactd/CompactdPlayer/commit/d618eace0547e3b44f2cd974c0e3516926061d18))
  - player not showing when media is already playing
  ([d48ee234](https://github.com/compactd/CompactdPlayer/commit/d48ee2348981513043dfba7d9fa92ff1a0a5970d))




## Features
  - sync progress notification
  ([96bbc2d8](https://github.com/compactd/CompactdPlayer/commit/96bbc2d8f20fe38ce2710ab44b337dd4bf77cdf1))
  - album sync exclusion indicator
  ([406dca35](https://github.com/compactd/CompactdPlayer/commit/406dca35356d325c6bf08ad3e0771744ea718552))
  - selective sync and disk space forecasting
  ([ce04c222](https://github.com/compactd/CompactdPlayer/commit/ce04c222c91ad30775315492a0ac325af27d333f))
  - hide track from tracklist
  ([0340569a](https://github.com/compactd/CompactdPlayer/commit/0340569ad9b35542b9e70ccd246de64e22dc51a3))
  - add menu to artist, album and track items
  ([9a06fca5](https://github.com/compactd/CompactdPlayer/commit/9a06fca5628cbc928e929c5ce09cf403ed2eed84))
  - cache artworks using glide
  ([b0052890](https://github.com/compactd/CompactdPlayer/commit/b00528902be12c60aff8ff63ebdcf2532bc7175b))
  - album activty shuffle and insert actions
  ([fceaf022](https://github.com/compactd/CompactdPlayer/commit/fceaf022e2a786f4e11d4831f1030b9f8e21aa0c))
  - artist and album activity transitions
  ([a24f03ca](https://github.com/compactd/CompactdPlayer/commit/a24f03ca0a10b69ad48873eb649cc228eaaa08c2))
  - playback loading inidcator
  ([55787ed9](https://github.com/compactd/CompactdPlayer/commit/55787ed9ca71a0826556b46874a96de62fb26256))
  - clear queue and stop playback menu
  ([ae6bcb10](https://github.com/compactd/CompactdPlayer/commit/ae6bcb10f47b501341b5b73dbf27d5d3e9ac2b8b))
  - shuffle playlist support
  ([62cbd386](https://github.com/compactd/CompactdPlayer/commit/62cbd386235d51e3881a833922d6c56cfb8dfa45))
  - artwork cover fallback
  ([6375ddba](https://github.com/compactd/CompactdPlayer/commit/6375ddbab90d4d4e209e855993a158d3b9219777))
  - improve artist and album activity design
  ([148583f5](https://github.com/compactd/CompactdPlayer/commit/148583f59420d2e6c536e521a930c2d22cef5a05))




## Refactor
  - remove pseudo cancel support from MediaCoverFetcher
  ([52908e1b](https://github.com/compactd/CompactdPlayer/commit/52908e1b238c861b24c6a64acf64eea98433a312))





## 1.0.0-alpha.0 ( Wed Dec 27 2017 01:07:43 GMT+0100 (Paris, Madrid) )


## Bug Fixes
  - MediaPlayerService leak when leaving app
  ([b5f19162](https://github.com/compactd/CompactdPlayer/commit/b5f1916254bac38114e3fcf3b5572130b0038e8e))
  - album and artist activity back and status bar dummy
  ([7a585326](https://github.com/compactd/CompactdPlayer/commit/7a585326c55ad7edf11de056fd3caf38111e50b7))
  - improve player layering
  ([781e03f1](https://github.com/compactd/CompactdPlayer/commit/781e03f19962a79b6aa5fe9257bab859922bfaca))
  - set progress as a percentage
  ([3485e52e](https://github.com/compactd/CompactdPlayer/commit/3485e52e585e8d5b5a3f4a2d04a1548b200ed6b5))
  - wait for media player to be initialized before openQueue()
  ([c6aaea10](https://github.com/compactd/CompactdPlayer/commit/c6aaea101cf8b83ef233430b3fbf06157455da9e))

## Features
  - basic offline support
  ([b117c02f](https://github.com/compactd/CompactdPlayer/commit/b117c02fe7756e6a4d268ff26d5606024ae6b720))
  - add sync activity
  ([f13b6de2](https://github.com/compactd/CompactdPlayer/commit/f13b6de2afd8e008fda25c366e580e69a3d4c8dd))
  - skip to track
  ([27de3085](https://github.com/compactd/CompactdPlayer/commit/27de3085daf5d977b107736f6153b1e5ff45c165))
  - add play to artist activity
  ([447cea80](https://github.com/compactd/CompactdPlayer/commit/447cea803afd9a10d82ba2665c60e34374b8ba37))
  - add PlaylistItemAdapter
  ([8b54ac8e](https://github.com/compactd/CompactdPlayer/commit/8b54ac8ecb7cc7998d8018ad3c2ac8352128551e))
  - working next and rewind player buttons
  ([f4ae3c7c](https://github.com/compactd/CompactdPlayer/commit/f4ae3c7c0f914feb8b276578175215a8d475b122))
  - playlist update
  ([c978492c](https://github.com/compactd/CompactdPlayer/commit/c978492c04df964b05fc11238187f7e5748a15c0))
  - add playlist and playback controls
  ([d07a356b](https://github.com/compactd/CompactdPlayer/commit/d07a356bf844ce5c58813e83e7237690be6ba931))
  - add top status bar dummy
  ([70853bbf](https://github.com/compactd/CompactdPlayer/commit/70853bbfbefeafa8ea29333ff9a458756f4e1d68))
  - playback controls
  ([65e1adb5](https://github.com/compactd/CompactdPlayer/commit/65e1adb52ba71792961926f145937ad4a03dd9cc))
  - cover view for player
  ([f740b9d8](https://github.com/compactd/CompactdPlayer/commit/f740b9d890b427c578eb20cfea2e4747204b947b))
  - feat:playback control using bottom fragment button
  ([f1d418e5](https://github.com/compactd/CompactdPlayer/commit/f1d418e524f449c207c15057d5c30d796ff0ebd6))
  - bottom panel showing current track title and playback state
  ([0c4ff897](https://github.com/compactd/CompactdPlayer/commit/0c4ff8977e67016c0336c9b182483c577ef2a44d))




## Refactor
  - remove OfflineItem and move sync code to CompactdTrack
  ([82f5c945](https://github.com/compactd/CompactdPlayer/commit/82f5c94585d1252f5ebcd6a1e04acea07bad5908))
  - split media and playback listener and create progress listener
  ([1ed6aedc](https://github.com/compactd/CompactdPlayer/commit/1ed6aedc801f7437d549826ef8ba8d8fcce63826))





---
<sub><sup>*Generated with [git-changelog](https://github.com/rafinskipg/git-changelog). If you have any problems or suggestions, create an issue.* :) **Thanks** </sub></sup>