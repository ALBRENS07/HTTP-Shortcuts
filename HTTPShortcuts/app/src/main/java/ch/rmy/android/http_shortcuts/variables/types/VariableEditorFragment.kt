package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.activities.VariableEditorActivity
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable

open class VariableEditorFragment : BaseFragment() {

    protected val controller by lazy { destroyer.own(Controller()) }
    protected val variables by lazy { controller.getVariables() }

    override fun onStart() {
        super.onStart()
        (activity as VariableEditorActivity).onFragmentStarted()
    }

    open fun updateViews(variable: Variable) {

    }

    open fun compileIntoVariable(variable: Variable) {

    }

    open fun validate() = true

}
