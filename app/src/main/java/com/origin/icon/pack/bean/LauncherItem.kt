package com.origin.icon.pack.bean

data class LauncherItem( val name: String,
                         val packageName: String,
                         val launcherColor: Int,
                         val isInstalled: Int = -1)