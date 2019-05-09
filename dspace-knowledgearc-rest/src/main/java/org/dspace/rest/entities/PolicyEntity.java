/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.rest.entities;

import org.dspace.authorize.ResourcePolicy;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PolicyEntity extends PolicyEntityId{

    private String action;
    private Object user;
    private Object group;
    private String startDate;
    private String endDate;

    public PolicyEntity() {
    }

    public PolicyEntity(ResourcePolicy c) throws SQLException {
        super(c);
        this.action = c.getActionText();
        this.user = c.getEPerson() != null ? new UserEntityTrim(c.getEPerson()) : null;
        this.group = c.getGroup() != null ? c.getGroup().getMemberGroups().length > 0 ? new GroupEntity(c.getGroup()) : new GroupEntityTrim(c.getGroup()) : null;

        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        this.startDate = c.getStartDate() == null ? null : simpleDateFormat.format(c.getStartDate());
        this.endDate = c.getEndDate() == null ? null : simpleDateFormat.format(c.getEndDate());
    }

    public String getAction() {
        return action;
    }

    public Object getUser() {
        return user;
    }

    public Object getGroup() {
        return group;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}