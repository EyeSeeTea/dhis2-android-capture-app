package org.dhis2.utils.session
// EyeSeeTea customization - Change Server URL

import dagger.Subcomponent

@Subcomponent(modules = [ChangeServerURLModule::class])
interface ChangeServerURLComponent {
    fun inject(changeServerUrlDialog: ChangeServerUrlDialog)
}