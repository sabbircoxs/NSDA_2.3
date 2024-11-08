package com.example.nsda_23
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookAdapter(
    private val onBookChecked: (Book) -> Unit,
    private val onEditClicked: (Book) -> Unit // Add this parameter
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private var books = emptyList<Book>()

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.text_view_title)
        private val authorTextView: TextView = itemView.findViewById(R.id.text_view_author)
        private val pagesTextView: TextView = itemView.findViewById(R.id.text_view_pages)
        private val editButton: Button = itemView.findViewById(R.id.button_edit)

        fun bind(book: Book) {
            titleTextView.text = book.title
            authorTextView.text = book.author
            pagesTextView.text = book.pages.toString()

            editButton.setOnClickListener {
                onEditClicked(book) // Trigger the edit callback
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val currentBook = books[position]
        holder.bind(currentBook)

        // Optional: Call the onBookChecked callback when the item is clicked
        holder.itemView.setOnClickListener {
            onBookChecked(currentBook)
        }
    }

    override fun getItemCount() = books.size

    fun setBooks(books: List<Book>) {
        this.books = books
        notifyDataSetChanged()
    }

    fun getBookAtPosition(position: Int): Book {
        return books[position]
    }
}

