
package com.example.borrowbuddy.ui.list;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.ListAdapter;

import com.example.borrowbuddy.R;
import com.example.borrowbuddy.data.model.Enums;
import com.example.borrowbuddy.data.model.Loan;
import com.example.borrowbuddy.domain.DateFmt;
import com.example.borrowbuddy.ui.edit.LoanEditFragment;
import com.example.borrowbuddy.data.Repository;
import android.widget.Toast;
import java.time.LocalDate;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;

public class LoanListFragment extends Fragment {
    private LoanListViewModel vm;
    private LoanAdapter adapter;
    private TextView empty;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Enable options menu in the fragment
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loan_list, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(LoanListViewModel.class);

        RecyclerView rv = v.findViewById(R.id.recycler);
        empty = v.findViewById(R.id.empty);
        adapter = new LoanAdapter();
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    rv.setAdapter(adapter);
    // 设置item淡出动画
    DefaultItemAnimator animator = new DefaultItemAnimator();
    animator.setRemoveDuration(300);
    animator.setAddDuration(200);
    rv.setItemAnimator(animator);

        // 滑动功能提示弹窗（每次进入都显示，直到用户点击“不再提示”）
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireContext());
    if (!sp.getBoolean("swipe_hint_never_show", false)) {
        new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.swipe_hint_title))
            .setMessage(getString(R.string.swipe_hint_message))
            .setPositiveButton(getString(R.string.swipe_hint_ok), null)
            .setNegativeButton(getString(R.string.swipe_hint_never), (d, w) -> sp.edit().putBoolean("swipe_hint_never_show", true).apply())
            .show();
    }

        // 添加滑动手势：右滑延后3天，左滑标记归还
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                Loan loan = adapter.getCurrentList().get(pos);
                RecyclerView recyclerView = (RecyclerView) viewHolder.itemView.getParent();
                // 滑动方向滚动消失动画
                viewHolder.itemView.animate()
                        .translationX(direction == ItemTouchHelper.LEFT ? -recyclerView.getWidth() : recyclerView.getWidth())
                        .setDuration(300)
                        .withEndAction(() -> {
                            if (direction == ItemTouchHelper.RIGHT) {
                                // 延后3天
                                if (loan.dueDate != null) {
                                    loan.dueDate = loan.dueDate.plusDays(3);
                                    new Repository(requireContext()).updateAsync(loan);
                                    Toast.makeText(requireContext(), "已延后3天", Toast.LENGTH_SHORT).show();
                                }
                            } else if (direction == ItemTouchHelper.LEFT) {
                                // 标记归还
                                new Repository(requireContext()).markReturnedAsync(loan.id);
                                Toast.makeText(requireContext(), "已标记为归还", Toast.LENGTH_SHORT).show();
                            }
                            // 重新拉取数据，防止UI与数据不同步
                            vm.getList().observe(getViewLifecycleOwner(), loans -> {
                                adapter.submitList(loans);
                                empty.setVisibility((loans==null||loans.isEmpty())?View.VISIBLE:View.GONE);
                            });
                        })
                        .start();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    Paint paint = new Paint();
                    int height = itemView.getBottom() - itemView.getTop();
                    float absDx = Math.abs(dX);
                    float maxDx = recyclerView.getWidth() * 0.5f;
                    float progress = Math.min(absDx / maxDx, 1f);
                    int alpha = (int) (progress * 255);
                    paint.setAlpha(alpha);
                    if (dX > 0) { // 右滑
                        paint.setColor(0xFF1976D2);
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), (float) itemView.getLeft() + dX, (float) itemView.getBottom(), paint);
                        paint.setColor(0xFFFFFFFF);
                        paint.setAlpha(alpha);
                        paint.setTextSize(48);
                        paint.setFakeBoldText(true);
                        String text = "延后3天";
                        float textWidth = paint.measureText(text);
                        float textX = itemView.getLeft() + 48;
                        float textY = itemView.getTop() + height / 2f + 18;
                        c.drawText(text, textX, textY, paint);
                    } else if (dX < 0) { // 左滑
                        paint.setColor(0xFF43A047);
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                        paint.setColor(0xFFFFFFFF);
                        paint.setAlpha(alpha);
                        paint.setTextSize(48);
                        paint.setFakeBoldText(true);
                        String text = "标记为归还";
                        float textWidth = paint.measureText(text);
                        float textX = itemView.getRight() - textWidth - 48;
                        float textY = itemView.getTop() + height / 2f + 18;
                        c.drawText(text, textX, textY, paint);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(rv);

        RadioGroup tabs = v.findViewById(R.id.group_type);
        tabs.setOnCheckedChangeListener((group, checkedId) -> {
            vm.setQuery("");
            vm.setType(checkedId == R.id.rb_loaned ? Enums.LoanType.LOANED : Enums.LoanType.BORROWED);
        });

        EditText search = v.findViewById(R.id.input_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s){ vm.setQuery(s.toString().trim()); }
        });

        vm.getList().observe(getViewLifecycleOwner(), loans -> {
            adapter.submitList(loans);
            empty.setVisibility((loans==null||loans.isEmpty())?View.VISIBLE:View.GONE);
        });

        adapter.setOnItemClickListener(loan -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, LoanEditFragment.editing(loan.id))
                    .addToBackStack(null).commit();
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_loan_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.filter_all) {
            vm.setFilterMode(LoanListViewModel.FilterMode.ALL);
            return true;
        } else if (itemId == R.id.filter_overdue) {
            vm.setFilterMode(LoanListViewModel.FilterMode.OVERDUE);
            return true;
        } else if (itemId == R.id.filter_due_this_week) {
            vm.setFilterMode(LoanListViewModel.FilterMode.DUE_THIS_WEEK);
            return true;
        } else if (itemId == R.id.action_settings) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new com.example.borrowbuddy.ui.settings.SettingsFragment())
                .addToBackStack(null)
                .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class LoanAdapter extends ListAdapter<Loan, LoanVH> {
        private OnItemClick click;

        protected LoanAdapter() {
            super(new DiffUtil.ItemCallback<Loan>() {
                @Override
                public boolean areItemsTheSame(@NonNull Loan oldItem, @NonNull Loan newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Loan oldItem, @NonNull Loan newItem) {
                    return oldItem.equals(newItem);
                }
            });
        }

        interface OnItemClick{ void onClick(Loan l); }
        public void setOnItemClickListener(OnItemClick c){ this.click=c; }

        @NonNull @Override public LoanVH onCreateViewHolder(@NonNull ViewGroup p, int v){
            return new LoanVH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_loan, p, false));
        }
        @Override public void onBindViewHolder(@NonNull LoanVH h,int pos){
            Loan l = getItem(pos);
            Context ctx = h.itemView.getContext();
            h.title.setText(l.title);
            h.person.setText(l.personName==null?"":l.personName);

            // Type indicator bar color
            int typeColorRes = (l.type == Enums.LoanType.LOANED)
                    ? R.color.color_type_loaned
                    : R.color.color_type_borrowed;
            h.typeIndicator.setBackgroundColor(ContextCompat.getColor(ctx, typeColorRes));

            // Due date chip text & background
            if (l.dueDate != null) {
                h.due.setText(DateFmt.date(l.dueDate));

                if (l.dueDate.isBefore(LocalDate.now())) {
                    // Overdue
                    h.due.setTextColor(ContextCompat.getColor(ctx, R.color.color_overdue));
                    h.due.setBackgroundColor(ContextCompat.getColor(ctx, R.color.color_due_chip_overdue_bg));
                } else {
                    long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(
                            LocalDate.now(), l.dueDate);
                    if (daysDiff <= 3) {
                        // Due soon
                            h.due.setTextColor(ContextCompat.getColor(ctx, R.color.color_primary_dark));
                            h.due.setBackgroundColor(ContextCompat.getColor(ctx, R.color.color_due_chip_soon_bg));
                    } else {
                        // Normal upcoming
                        h.due.setTextColor(ContextCompat.getColor(ctx, R.color.color_due_default));
                        h.due.setBackgroundColor(ContextCompat.getColor(ctx, R.color.color_due_chip_normal_bg));
                    }
                }
            } else {
                h.due.setText(R.string.due_date_not_set);
                h.due.setTextColor(ContextCompat.getColor(ctx, R.color.color_due_default));
                h.due.setBackgroundColor(ContextCompat.getColor(ctx, R.color.color_due_chip_normal_bg));
            }

            h.itemView.setOnClickListener(v-> { if(click!=null) click.onClick(l); });
        }
    }

    private static class LoanVH extends RecyclerView.ViewHolder{
        TextView title, person, due;
        View typeIndicator;
        public LoanVH(@NonNull View item){ super(item);
            title=item.findViewById(R.id.tv_title);
            person=item.findViewById(R.id.tv_person);
            due=item.findViewById(R.id.tv_due);
            typeIndicator=item.findViewById(R.id.view_type_indicator);
        }
    }
}
