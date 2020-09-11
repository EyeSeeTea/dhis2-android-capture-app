package org.dhis2.core.types

sealed class TreeNode<T>(val content: T) {
    protected var parent: TreeNode<*>? = null

    val level: Int
        get() {
            return if (parent == null) {
                0
            } else {
                val parentLevel = parent!!.level
                parentLevel + 1
            }
        }

    class Node<N>(
        content: N,
        initialChildren: List<TreeNode<*>> = mutableListOf()
    ) : TreeNode<N>(content) {
        private val internalChildren: MutableList<TreeNode<*>> = initialChildren.toMutableList()

        init {
            children.forEach {
                it.parent = this
            }
        }

        val children: List<TreeNode<*>>
            get() {
                return internalChildren.toList()
            }

        fun node(content: N, initialize: (Node<N>.() -> Unit)? = null) {
            val child = Node(content)

            addChild(child)
            if (initialize != null) {
                child.initialize()
            }
        }

        fun <L> leaf(content: L) {
            addChild(Leaf(content))
        }

        fun addChild(node: TreeNode<*>) {
            internalChildren.add(node)
            node.parent = this
        }

/*        fun depthFilter( predicate: (TreeNode<*>)->Boolean ): Node<N>{
            return Node(this.content,this.children.filter {
                predicate(it)
            }, this.expanded)
        }*/

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Node<*>

            if (internalChildren != other.internalChildren) return false
            if (content != other.content) return false

            return true
        }

        override fun hashCode(): Int {
            var result = content.hashCode()
            result = 31 * result + internalChildren.hashCode()
            return result
        }
    }

    class Leaf<L>(content: L) : TreeNode<L>(content) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Leaf<*>

            if (content != other.content) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}

fun <N> root(content: N, initialize: (TreeNode.Node<N>.() -> Unit)? = null): TreeNode.Node<N> {
    val node = TreeNode.Node(content)
    if (initialize != null) {
        node.initialize()
    }
    return node
}
