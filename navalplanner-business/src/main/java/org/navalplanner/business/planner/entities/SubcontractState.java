/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.business.planner.entities;

import org.navalplanner.business.i18n.I18nHelper;

/**
 * Possible states of a {@link SubcontractedTaskData}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public enum SubcontractState {
    PENDING(_("Pending"), true), FAILED_SENT(_("Failed sent"), true), SUCCESS_SENT(
            _("Success sent"), false);

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    private String name;
    private boolean sendable;

    private SubcontractState(String name, boolean sendable) {
        this.name = name;
        this.sendable = sendable;
    }

    @Override
    public String toString() {
        return I18nHelper._(name);
    }

    public boolean isSendable() {
        return sendable;
    }

}