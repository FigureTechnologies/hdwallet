package tech.figure.hdwallet.common.chararray

open class CharArrayBuffer : Appendable, MutableCollection<CharArray> {
    private val buffer = mutableListOf<CharArray>()

    // Implement Appendable
    override fun append(p0: Char): Appendable = apply { buffer += CharArray(1).also { it[0] = p0 } }
    override fun append(p0: CharSequence): Appendable = apply { buffer += p0.toList().toCharArray() }
    override fun append(p0: CharSequence, offset: Int, end: Int): Appendable = apply {
        if (offset !in p0.indices || end !in p0.indices || end < offset) {
            throw IndexOutOfBoundsException("start $offset, end $end, length ${p0.length}")
        }
        buffer += p0.slice(offset until end).toList().toCharArray()
    }

    // Implement MutableCollection
    override val size: Int = buffer.size
    override fun contains(element: CharArray): Boolean = buffer.contains(element)
    override fun containsAll(elements: Collection<CharArray>): Boolean = buffer.containsAll(elements)
    override fun isEmpty(): Boolean = buffer.isEmpty()
    override fun add(element: CharArray): Boolean = buffer.add(element)
    override fun addAll(elements: Collection<CharArray>): Boolean = buffer.addAll(elements)
    override fun clear() = buffer.clear()
    override fun iterator(): MutableIterator<CharArray> = buffer.iterator()
    override fun remove(element: CharArray): Boolean = buffer.remove(element)
    override fun removeAll(elements: Collection<CharArray>): Boolean = buffer.removeAll(elements)
    override fun retainAll(elements: Collection<CharArray>): Boolean = buffer.retainAll(elements)

    fun toCharArray(): CharArray = if (buffer.isEmpty()) CharArray(0) else buffer.reduce(CharArray::plus)
}
