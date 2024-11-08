package com.example.nsda_23

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper

class MainActivity : AppCompatActivity() {
    private lateinit var bookViewModel: BookViewModel
    private lateinit var bookAdapter: BookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button_add_book).setOnClickListener {
            showAddBookDialog()
        }

        setupSwipeToDelete()

        // Set up RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        bookAdapter = BookAdapter(
            { book -> onBookChecked(book) },
            { book -> showEditBookDialog(book) } // Pass the edit callback
        )
        recyclerView.adapter = bookAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set up ViewModel
        val bookDao = BookDatabase.getDatabase(application).bookDao()
        val repository = BookRepository(bookDao)
        val viewModelFactory = BookViewModelFactory(repository)
        bookViewModel = viewModels<BookViewModel> { viewModelFactory }.value

        // Observe the LiveData
        bookViewModel.allBooks.observe(this, Observer { books ->
            books?.let { bookAdapter.setBooks(it) }
        })

        val searchEditText = findViewById<EditText>(R.id.edit_text_search)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    bookViewModel.searchBooks(query).observe(this@MainActivity, Observer { books ->
                        bookAdapter.setBooks(books)
                    })
                } else {
                    bookViewModel.allBooks.observe(this@MainActivity, Observer { books ->
                        bookAdapter.setBooks(books)
                    })
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun onBookChecked(book: Book) {
        bookViewModel.update(book)
    }

    private fun showAddBookDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_book, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.edit_text_title)
        val authorEditText = dialogView.findViewById<EditText>(R.id.edit_text_author)
        val pagesEditText = dialogView.findViewById<EditText>(R.id.edit_text_pages)

        AlertDialog.Builder(this)
            .setTitle("Add New Book")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->
                val title = titleEditText.text.toString()
                val author = authorEditText.text.toString()
                val pages = pagesEditText.text.toString().toIntOrNull() ?: 0
                if (title.isNotBlank() && author.isNotBlank() && pages > 0) {
                    val newBook = Book(title = title, author = author, pages = pages)
                    bookViewModel.insert(newBook)
                } else {
                    Toast.makeText(this, "Please enter valid details", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun showEditBookDialog(book: Book) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_book, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.edit_text_title)
        val authorEditText = dialogView.findViewById<EditText>(R.id.edit_text_author)
        val pagesEditText = dialogView.findViewById<EditText>(R.id.edit_text_pages)

        // Populate fields with current book data
        titleEditText.setText(book.title)
        authorEditText.setText(book.author)
        pagesEditText.setText(book.pages.toString())

        AlertDialog.Builder(this)
            .setTitle("Edit Book")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Update") { _, _ ->
                val updatedTitle = titleEditText.text.toString()
                val updatedAuthor = authorEditText.text.toString()
                val updatedPages = pagesEditText.text.toString().toIntOrNull() ?: 0
                if (updatedTitle.isNotBlank() && updatedAuthor.isNotBlank() && updatedPages > 0) {
                    val updatedBook = book.copy(title = updatedTitle, author = updatedAuthor, pages = updatedPages)
                    bookViewModel.update(updatedBook)
                } else {
                    Toast.makeText(this, "Please enter valid details", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val bookToDelete = bookAdapter.getBookAtPosition(position)
                bookViewModel.delete(bookToDelete)
                Toast.makeText(this@MainActivity, "Book deleted", Toast.LENGTH_SHORT).show()
            }
        })
        itemTouchHelper.attachToRecyclerView(findViewById(R.id.recycler_view))
    }
}
