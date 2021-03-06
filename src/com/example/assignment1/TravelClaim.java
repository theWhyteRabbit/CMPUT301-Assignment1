/*
 * Copyright 2015 Morgan Redshaw
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.example.assignment1;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.util.Pair;

@SuppressWarnings("rawtypes")
public class TravelClaim extends FModel<FView> {
    private String name;
    private Calendar startDate, endDate;
    private String description;
    private List<TravelExpense> allExpenses;

    private TravelClaimStates currentState;

    public TravelClaim() {
        name = "";
        startDate = new GregorianCalendar();
        endDate = new GregorianCalendar();
        description = "";
        allExpenses = new ArrayList<TravelExpense>();
        currentState = TravelClaimStates.IN_PROGRESS;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String newName) {
        if (!name.equals(newName) && mayBeEdited()) {
            name = newName;
            updated();
        }
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String newDescription) {
        if (!description.equals(newDescription) && mayBeEdited()) {
            description = newDescription;
            updated();
        }
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar newStartDate) {
        if (!startDate.equals(newStartDate) && mayBeEdited()) {

            // Not possible for end date to be less than start date
            if (Utilities.calLessThan(endDate, newStartDate)) {
                startDate = endDate;
            } else {
                startDate = newStartDate;
            }
            updated();
        }
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar newEndDate) {
        if (!endDate.equals(newEndDate) && mayBeEdited()) {

            // Not possible for end date to be less than start date
            if (Utilities.calLessThan(newEndDate, startDate)) {
                endDate = startDate;
            } else {
                endDate = newEndDate;
            }
            updated();
        }
    }

    public TravelClaimStates getState() {
        return currentState;
    }

    public void setState(TravelClaimStates newState) {
        if (isValidStateChange(newState)) {
            currentState = newState;

            // EDIT: Need to possibly do email
            updated();
        }
    }

    public boolean isValidStateChange(TravelClaimStates newState) {
        switch (currentState) {
        case IN_PROGRESS:
            // Should fall through
        case RETURNED:
            return newState == TravelClaimStates.SUBMITTED;

        case SUBMITTED:
            return newState == TravelClaimStates.APPROVED || newState == TravelClaimStates.RETURNED;

        default:
            return false;
        }
    }

    public boolean mayBeEdited() {
        return (currentState == TravelClaimStates.IN_PROGRESS || currentState == TravelClaimStates.RETURNED);
    }

    public void createExpense() {
        allExpenses.add(new TravelExpense());
        updated();
    }

    // They will be sorted by date
    public List<TravelExpense> getAllExpenses() {
        return allExpenses;
    }

    public int getExpensePosition(TravelExpense expense) {
        return allExpenses.indexOf(expense);
    }

    public void deleteExpense(TravelExpense expense) {
        allExpenses.remove(expense);
        updated();
    }

    private void updated() {
        TravelClaimOwner owner = TravelApplication.getMainOwner();
        owner.dataHasBeenUpdated();
        notifyViews();
    }

    public List<Pair<String, Float>> getCurrencyInformation() {
        List<Pair<String, Float>> allPayments = new ArrayList<Pair<String, Float>>();
        for (TravelExpense expense : allExpenses) {
            allPayments.add(new Pair<String, Float>(expense.getCurrency().getCurrencyCode(), expense.getAmount()));
        }

        return Utilities.mergeAllDuplicatedPayments(allPayments);
    }

}
