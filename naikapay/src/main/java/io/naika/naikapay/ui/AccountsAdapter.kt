package io.naika.naikapay.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.RecyclerView
import io.naika.naikapay.R
import io.naika.naikapay.convertBigIntBalanceToDouble
import io.naika.naikapay.toSummarisedAddress

class AccountsAdapter(
    val listener: OnAccountAdapterInteraction
) : RecyclerView.Adapter<AccountsAdapter.AccountsViewHolder>() {

    private var data: MutableList<AccountUIModel> = mutableListOf()

    fun setData(accountUIModel: List<AccountUIModel>) {
        data.clear()
        data.addAll(accountUIModel)
        notifyItemRangeChanged(0, accountUIModel.size)
    }

    fun addData(accountUIModel: List<AccountUIModel>) {
        data.addAll(accountUIModel)
        notifyItemRangeInserted(0, accountUIModel.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountsViewHolder {
        return AccountsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.account_row_item, parent, false),
            listener
        )
    }

    override fun onBindViewHolder(holder: AccountsViewHolder, position: Int) {
        val account = data[position]
        holder.onBind(account, position)
    }

    override fun getItemCount(): Int {
        return data.size
    }


    class AccountsViewHolder(itemView: View, private val listener: OnAccountAdapterInteraction) :
        RecyclerView.ViewHolder(itemView) {

        private val hashAddress: TextView =
            itemView.findViewById(R.id.account_hash_address_text_view)
        private val balance: TextView = itemView.findViewById(R.id.account_balance_text_view)
        private val progressBar: ContentLoadingProgressBar =
            itemView.findViewById(R.id.balance_progress_bar)

        fun onBind(account: AccountUIModel, position: Int) {

            itemView.setOnClickListener {
                listener.onAccountClicked(account)
            }

            hashAddress.text = toSummarisedAddress(account.account.address.hex).toLowerCase()
            if (account.balance == null) {
                progressBar.show()
                balance.visibility = View.INVISIBLE
            } else {
                progressBar.hide()
                balance.visibility = View.VISIBLE
                balance.text = String.format("%.6f", convertBigIntBalanceToDouble(account.balance))
            }


        }

    }

    interface OnAccountAdapterInteraction {
        fun onAccountClicked(account: AccountUIModel)
    }
}