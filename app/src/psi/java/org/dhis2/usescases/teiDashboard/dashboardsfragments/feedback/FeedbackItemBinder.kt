package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.core.ui.tree.TreeAdapterBinder
import org.dhis2.core.types.TreeNode

class FeedbackItemBinder : TreeAdapterBinder(FeedbackItem::class.java) {
    override val layoutId: Int
        get() = R.layout.item_feedback

    override fun provideViewHolder(itemView: View): RecyclerView.ViewHolder {
        return ViewHolder(itemView)
    }

    override fun bindView(
        holder: RecyclerView.ViewHolder,
        node: TreeNode<*>,
        isExpanded: Boolean
    ) {
        with(holder as ViewHolder) {
            val feedbackItem: FeedbackItem = node.content as FeedbackItem

            renderColor(itemView, node)
            renderName(name, feedbackItem, node)
            renderValue(value, feedbackItem, feedbackItem.value)
            renderArrow(arrow, node, isExpanded)
        }
    }

    private fun renderColor(
        itemView: View,
        node: TreeNode<*>
    ) {
        if (node is TreeNode.Leaf) {
            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.bg_gray_faf
                )
            )
        } else {
            itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun renderName(
        nameView: TextView,
        feedbackItem: FeedbackItem,
        node: TreeNode<*>
    ) {
        if (node is TreeNode.Node && node.children.isNotEmpty() && node.children[0] is TreeNode.Node) {
            TextViewCompat.setTextAppearance(
                nameView,
                R.style.TextAppearance_MaterialComponents_Body1
            )
        } else {
            TextViewCompat.setTextAppearance(
                nameView,
                R.style.TextAppearance_MaterialComponents_Body2
            )
        }

        nameView.text = (feedbackItem.name)
    }

    private fun renderValue(
        valueView: TextView,
        feedbackItem: FeedbackItem,
        value: FeedbackItemValue?
    ) {
        if (feedbackItem.value == null || value?.data.isNullOrBlank()) {
            valueView.visibility = View.GONE
        } else {
            valueView.text = value?.data

            if (value?.color != null) {
                valueView.setBackgroundColor(Color.parseColor(value.color))
            } else {
                valueView.setBackgroundColor(Color.TRANSPARENT)
            }

            valueView.visibility = View.VISIBLE
        }
    }

    private fun renderArrow(
        arrow: ImageView,
        node: TreeNode<*>,
        isExpanded: Boolean
    ) {
        if (node is TreeNode.Node && isExpanded) {
            arrow.setImageResource(R.drawable.ic_arrow_up)
        } else {
            arrow.setImageResource(R.drawable.ic_arrow_down)
        }

        arrow.visibility =
            if (node is TreeNode.Node && node.children.isNotEmpty()) View.VISIBLE else View.INVISIBLE
    }

    internal class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val name: TextView = rootView.findViewById(R.id.name)
        val value: TextView = rootView.findViewById(R.id.value)
        val arrow: ImageView = rootView.findViewById(R.id.arrow)
    }
}