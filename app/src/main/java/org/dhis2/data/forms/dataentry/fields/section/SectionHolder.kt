package org.dhis2.data.forms.dataentry.fields.section

import android.animation.Animator
import android.view.View
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.ObservableField
import io.reactivex.processors.FlowableProcessor
import org.dhis2.data.forms.dataentry.fields.FormViewHolder
import org.dhis2.databinding.FormSectionBinding

class SectionHolder(
    private val formBinding: FormSectionBinding,
    private val selectedSection: ObservableField<String>,
    private val sectionProcessor: FlowableProcessor<String>
) : FormViewHolder(formBinding), View.OnClickListener {

    private lateinit var viewModel: SectionViewModel

    init {
        selectedSection.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(
                sender: Observable,
                propertyId: Int
            ) {
                animateArrow()
            }
        })
        formBinding.root.setOnClickListener(this)
    }

    fun update(viewModel: SectionViewModel) {
        this.viewModel = viewModel
        formBinding.apply {
            sectionName.text = viewModel.label()
            openIndicator.visibility = if (viewModel.isOpen) View.VISIBLE else View.GONE
            sectionFieldsInfo.text = String.format(
                "%s/%s",
                viewModel.completedFields(),
                viewModel.totalFields()
            )
        }
    }

    override fun dispose() {}

    override fun onClick(v: View) {
        if(!viewModel.isOpen) {
            selectedSection.set(viewModel.uid())
            sectionProcessor.onNext(viewModel.uid())
        }else{
            selectedSection.set("")
            sectionProcessor.onNext("")
        }
    }

    private fun animateArrow() {
        val isSelected = selectedSection.get() == viewModel.uid()
        if (isSelected) {
            formBinding.openIndicator.rotation = -45f
        }
        formBinding.openIndicator.animate()
            .scaleY(if (isSelected) 1f else 0f)
            .scaleX(if (isSelected) 1f else 0f)
            .rotation(if (isSelected) 0f else -45f)
            .setDuration(200)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    formBinding.openIndicator.visibility =
                        if (viewModel.isOpen) View.VISIBLE else View.GONE
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            .start()
    }


}