package com.omar.pcconnector.ui.preferences.server

import com.omar.pcconnector.preferences.ServerPreferences

interface ServerPreferencesActions {

    fun setStartPath(path: String)
    fun deleteDevice()
    fun setAsDefault()
    fun toggleShowHiddenResource()
    fun changeFileSystemSortCriteria(displayOrder: ServerPreferences.FileSystemSortCriteria)
    fun changeFilesAndFoldersSeparation(value: ServerPreferences.FoldersAndFilesSeparation)
    fun toggleSendPhoneNotificationsToServer()
    fun setNotificationsExcludedPackages(packageName: List<String>)
}