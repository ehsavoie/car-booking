/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.booking;

import java.util.List;
import java.util.Objects;

public class FraudResponse {

    private String customerName;
    private String customerSurname;
    private boolean fraudDetected;
    private List<String> bookingIds;

    public FraudResponse() {
    }

    public String getCustomerName() {
        return this.customerName;
    }

    public String getCustomerSurname() {
        return this.customerSurname;
    }

    public boolean isFraudDetected() {
        return this.fraudDetected;
    }

    public List<String> getBookingIds() {
        return this.bookingIds;
    }

    public void setCustomerName(final String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerSurname(final String customerSurname) {
        this.customerSurname = customerSurname;
    }

    public void setFraudDetected(final boolean fraudDetected) {
        this.fraudDetected = fraudDetected;
    }

    public void setBookingIds(final List<String> bookingIds) {
        this.bookingIds = bookingIds;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.customerName);
        hash = 17 * hash + Objects.hashCode(this.customerSurname);
        hash = 17 * hash + (this.fraudDetected ? 1 : 0);
        hash = 17 * hash + Objects.hashCode(this.bookingIds);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FraudResponse other = (FraudResponse) obj;
        if (this.fraudDetected != other.fraudDetected) {
            return false;
        }
        if (!Objects.equals(this.customerName, other.customerName)) {
            return false;
        }
        if (!Objects.equals(this.customerSurname, other.customerSurname)) {
            return false;
        }
        return Objects.equals(this.bookingIds, other.bookingIds);
    }

    @Override
    public String toString() {
        return "FraudResponse{" + "customerName=" + customerName + ", customerSurname=" + customerSurname + ", fraudDetected=" + fraudDetected + ", bookingIds=" + bookingIds + '}';
    }

}
