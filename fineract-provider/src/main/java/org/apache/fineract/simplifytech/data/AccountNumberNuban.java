/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.simplifytech.data;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import static org.apache.fineract.simplifytech.data.ApplicationPropertiesConstant.NIBSS_SORTCODE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 *
 * @author Olakunle.Thompson Generate a 10 digit NUBAN account number
 */
@Slf4j
public class AccountNumberNuban {

    private final int TEN = 10;
    private final int SEVEN = 7;
    private final int THREE = 3;
    private String sortCode;

    private String serialNumber;

    private final Environment environment;

    @Autowired
    public AccountNumberNuban(String accountNumber, Environment environment, String sortCode) {
        this.serialNumber = accountNumber;
        this.environment = environment;
        this.sortCode = sortCode;
    }

    public String getAccountNumber() {
        return serialNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.serialNumber = accountNumber;
    }

    public String NUBAN() {

        if (this.serialNumber.length() != 9) {
            // auto generate serialNumber must always be 9 digits to meet NIBSS requirements
            throw new IllegalArgumentException("Error in generating account number format.");
        }

        // assign the first 9 digit
        StringBuilder accountNumber = new StringBuilder();
        accountNumber.append(this.serialNumber);
        try {

            Integer algoThree = THREE;
            Integer algoSeven = SEVEN;
            Integer algoTen = TEN;
            if (StringUtils.isBlank(sortCode)) {
                //use default in app properties
                sortCode = this.environment.getProperty(NIBSS_SORTCODE);
            }

            String sortCodeSerialNumber = sortCode + this.serialNumber;
            int count = 0;

            int calculate = 0;
            /*
             * System.out.println("algoThree: " + algoThree); System.out.println("algoSeven: " + algoSeven);
             * System.out.println("algoTen: " + algoTen); System.out.println("sortCode: " + sortCode);
             * System.out.println("this.serialNumber: " + this.serialNumber);
             * System.out.println("sortCodeSerialNumber: " + sortCodeSerialNumber);
             */
            for (int i = 0; i < sortCodeSerialNumber.length(); i++) {
                if (i % 3 == 0) {
                    count = 1;
                } else {
                    count++;
                }
                // System.out.println(i + " = " + Integer.parseInt(String.valueOf(sortCodeSerialNumber.charAt(i))));
                Integer value = Integer.valueOf(String.valueOf(sortCodeSerialNumber.charAt(i)));

                if (count % 2 == 0) {
                    calculate += (value * algoSeven);
                    // System.out.println(value + " * " + algoSeven + " = " + (value * algoSeven));
                } else {
                    calculate += (value * algoThree);
                    // System.out.println(value + " * " + algoThree + " = " + (value * algoThree));
                }
            }
            // System.out.println("calculate: " + calculate);
            int moduloOfResult = calculate % algoTen;
            int checkDigit = algoTen - moduloOfResult;
            if (checkDigit == 10) {
                accountNumber.append(0);
            } else {
                accountNumber.append(Math.abs(checkDigit));
            }
            // System.out.println("accountNumber: " + accountNumber);

        } catch (NumberFormatException e) {
            log.error("NUBAN ERROR: ", e);
            throw new NumberFormatException("Check the config properties for account number generation.");
        }
        return accountNumber.toString();
    }

    /*
     * public static void main(String[] args) { String val = new AccountNumberNuban("000000022", null).NUBAN();
     * System.out.println("val: " + val); }
     */
}
