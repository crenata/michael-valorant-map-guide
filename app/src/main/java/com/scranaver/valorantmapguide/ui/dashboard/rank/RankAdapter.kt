package com.scranaver.valorantmapguide.ui.dashboard.rank

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scranaver.valorantmapguide.databinding.RanksBinding

class RankAdapter(private var rankList: List<Rank>): RecyclerView.Adapter<RankAdapter.ViewHolder>() {
    inner class ViewHolder(val rankBinding: RanksBinding): RecyclerView.ViewHolder(rankBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rankBinding = RanksBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(rankBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            val rankItem: Rank = rankList[position]
            with(rankItem) {
                rankBinding.rank.text = this.rank
            }
        }
    }

    override fun getItemCount(): Int {
        return rankList.size
    }
}