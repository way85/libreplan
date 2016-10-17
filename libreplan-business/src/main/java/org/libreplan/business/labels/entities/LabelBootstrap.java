/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 CafédeRed Solutions, S.L.
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

package org.libreplan.business.labels.entities;

import org.libreplan.business.common.daos.IEntitySequenceDAO;
import org.libreplan.business.common.entities.EntityNameEnum;
import org.libreplan.business.labels.daos.ILabelTypeDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the default {@link Label}.
 *
 * @author Ignacio Díaz Teijido <ignacio.diaz@cafedered.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class LabelBootstrap implements ILabelBootstrap {

    @Autowired
    private ILabelTypeDAO labelTypeDAO;

    @Autowired
    private IEntitySequenceDAO entitySequenceDAO;

    @Override
    @Transactional
    public void loadRequiredData() {
        if (labelTypeDAO.getAll().isEmpty()) {
            LabelType priorityType = LabelType.create("Priority");
            priorityType.setCodeAutogenerated(true);
            priorityType.setCode(entitySequenceDAO.getNextEntityCodeWithoutTransaction(EntityNameEnum.LABEL));

            for (PredefinedLabels predefinedLabel : PredefinedLabels.values()) {
                Label label = predefinedLabel.getLabel();
                priorityType.addLabel(label);
            }
            priorityType.generateLabelCodes(entitySequenceDAO.getNumberOfDigitsCode(EntityNameEnum.LABEL));

            labelTypeDAO.save(priorityType);
        }
    }

}