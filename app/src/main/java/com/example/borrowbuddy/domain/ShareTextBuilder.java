package com.example.borrowbuddy.domain;

import com.example.borrowbuddy.data.model.Enums;
import com.example.borrowbuddy.data.model.Loan;
import java.time.ZoneId;

public class ShareTextBuilder {
    public static String build(Loan l){
        String who = (l.personName==null||l.personName.trim().isEmpty())?"":l.personName+"，";
        String when = l.createdAt.atZone(ZoneId.systemDefault()).toLocalDate().toString();
        String due = l.dueDate.toString();
        String amount = l.amountCents==null? "": "（约 "+(l.amountCents/100.0)+(l.currency==null?"":(" "+l.currency))+"）";
        if (l.type== Enums.LoanType.LOANED)
            return who+"我在 "+when+" 借给你“"+l.title+"”"+amount+"，到期日是 "+due+"。如果方便请归还；若需要更多时间也没关系，告诉我一个合适日期即可。谢谢！";
        else
            return who+"我在 "+when+" 向你借了“"+l.title+"”"+amount+"，到期日是 "+due+"。我将按时归还，如果你更合适的时间也请告诉我。感谢！";
    }
}
