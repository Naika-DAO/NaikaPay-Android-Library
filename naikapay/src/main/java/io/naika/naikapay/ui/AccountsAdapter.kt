package io.naika.naikapay.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.naika.naikapay.R
import org.ethereum.geth.Account

class AccountsAdapter(
    private val accounts: List<Account>,
    val listener: OnAccountAdapterInteraction
) : RecyclerView.Adapter<AccountsAdapter.AccountsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountsViewHolder {
        return AccountsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.account_row_item, parent, false),
            listener
        )
    }

    override fun onBindViewHolder(holder: AccountsViewHolder, position: Int) {
        val account = accounts[position]
        holder.onBind(account, position)
    }

    override fun getItemCount(): Int {
        return accounts.size
    }


    class AccountsViewHolder(itemView: View, private val listener: OnAccountAdapterInteraction) :
        RecyclerView.ViewHolder(itemView) {

        private val hashAddress: TextView =
            itemView.findViewById(R.id.account_hash_address_text_view)

        fun onBind(account: Account, position: Int) {

            itemView.setOnClickListener {
                listener.onAccountClicked(account)
            }

            hashAddress.text = account.address.hex
            Log.d("Adapter", account.url)

        }

    }

    interface OnAccountAdapterInteraction {
        fun onAccountClicked(account: Account)
    }
}