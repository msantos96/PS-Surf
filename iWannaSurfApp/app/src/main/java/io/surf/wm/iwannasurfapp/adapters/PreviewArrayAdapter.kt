package io.surf.wm.iwannasurfapp.adapters

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import io.surf.wm.iwannasurfapp.R
import io.surf.wm.iwannasurfapp.model.Dtos.*

class PreviewArrayAdapter(private val myDataset: Array<DayRate>) : RecyclerView.Adapter<PreviewArrayAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class ViewHolder(val constraintLayout: ConstraintLayout) : RecyclerView.ViewHolder(constraintLayout) {
        var day: TextView
        var minRate: TextView
        var maxRate: TextView
        init {
            day = constraintLayout.findViewById(R.id.day)
            minRate = constraintLayout.findViewById(R.id.min_rank)
            maxRate = constraintLayout.findViewById(R.id.max_rank)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewArrayAdapter.ViewHolder {
        // create a new view
        val constraintLayout = LayoutInflater.from(parent.context).inflate(R.layout.week_preview, parent, false) as ConstraintLayout

        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(constraintLayout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.day.text = myDataset[position].day
        holder.minRate.text = myDataset[position].rate.min.toString()
        holder.maxRate.text = myDataset[position].rate.max.toString()

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}