import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.afifit.R
import com.sendbird.android.User

class ChannelCreateAdapter(private val listener: OnItemCheckedChangeListener) :
    RecyclerView.Adapter<ChannelCreateAdapter.UserHolder>(){

    interface OnItemCheckedChangeListener {
        fun onItemChecked(user: User, checked: Boolean)
    }

    private var users: MutableList<User> = ArrayList()

    companion object {
        private val selectedUsers = ArrayList<String>()
        private val sparseArray = SparseBooleanArray()
    }

    fun addUsers(users: MutableList<User>) {
        this.users = users
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UserHolder(layoutInflater.inflate(R.layout.item_create, parent, false))
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        holder.bindViews(users[position], position, listener)
    }

    class UserHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val checkbox = view.findViewById<CheckBox>(R.id.checkbox_create)
        private val userId = view.findViewById<TextView>(R.id.text_create_user)

        fun bindViews(user: User, position: Int, listener: OnItemCheckedChangeListener) {
            userId.text = user.userId
            checkbox.isChecked = sparseArray.get(position, false)

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                listener.onItemChecked(user, isChecked)

                if (isChecked) {
                    selectedUsers.add(user.userId)
                    sparseArray.put(position, true)
                } else {
                    selectedUsers.remove(user.userId)
                    sparseArray.put(position, false)
                }
            }
        }
    }
}
