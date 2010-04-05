/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.resources.machine;

import static org.navalplanner.web.I18nHelper._;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidValue;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.web.calendars.BaseCalendarEditionController;
import org.navalplanner.web.calendars.IBaseCalendarModel;
import org.navalplanner.web.common.ConstraintChecker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.navalplanner.web.common.components.finders.FilterPair;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.costcategories.ResourcesCostCategoryAssignmentController;
import org.navalplanner.web.resources.search.ResourcePredicate;
import org.navalplanner.web.resources.worker.CriterionsController;
import org.navalplanner.web.resources.worker.CriterionsMachineController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Window;
import org.navalplanner.web.resources.worker.WorkerCRUDController.LimitedResourceEnum;

/**
 * Controller for {@link Machine} resource <br />
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class MachineCRUDController extends GenericForwardComposer {

    private Window listWindow;

    private Window editWindow;

    private IMachineModel machineModel;

    private IURLHandlerRegistry URLHandlerRegistry;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Component configurationUnits;

    private CriterionsMachineController criterionsController;

    private MachineConfigurationController configurationController;

    private ResourcesCostCategoryAssignmentController resourcesCostCategoryAssignmentController;

    private Grid listing;

    private Datebox filterStartDate;

    private Datebox filterFinishDate;

    private Combobox filterLimitedResource;

    private Textbox txtfilter;

    private BandboxMultipleSearch bdFilters;

    private static final Log LOG = LogFactory
            .getLog(MachineCRUDController.class);

    public MachineCRUDController() {

    }

    private BaseCalendarsComboitemRenderer baseCalendarsComboitemRenderer = new BaseCalendarsComboitemRenderer();

    public List<Machine> getMachines() {
        return machineModel.getMachines();
    }

    public Machine getMachine() {
        return machineModel.getMachine();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);
        setupCriterionsController();
        setupConfigurationController();
        setupResourcesCostCategoryAssignmentController(comp);
        showListWindow();
        initFilterComponent();
    }

    private void showListWindow() {
        getVisibility().showOnly(listWindow);
    }

    private void initFilterComponent() {
        this.filterFinishDate = (Datebox) listWindow
                .getFellowIfAny("filterFinishDate");
        this.filterStartDate = (Datebox) listWindow
                .getFellowIfAny("filterStartDate");
        this.filterLimitedResource = (Combobox) listWindow
                .getFellowIfAny("filterLimitedResource");
        this.bdFilters = (BandboxMultipleSearch) listWindow
                .getFellowIfAny("bdFilters");
        this.txtfilter = (Textbox) listWindow.getFellowIfAny("txtfilter");
        this.listing = (Grid) listWindow.getFellowIfAny("listing");
        clearFilterDates();
    }

    private OnlyOneVisible getVisibility() {
        if (visibility == null) {
            visibility = new OnlyOneVisible(listWindow, editWindow);
        }
        return visibility;
    }

    private void setupCriterionsController() throws Exception {
        final Component comp = editWindow.getFellowIfAny("criterionsContainer");
        criterionsController = new CriterionsMachineController();
        criterionsController.doAfterCompose(comp);
    }

    private void setupConfigurationController() throws Exception {
        configurationUnits = editWindow.getFellow("configurationUnits");
        configurationController = (MachineConfigurationController) configurationUnits
                .getVariable("configurationController", true);
    }

    private void setupResourcesCostCategoryAssignmentController(Component comp)
    throws Exception {
        Component costCategoryAssignmentContainer =
            editWindow.getFellowIfAny("costCategoryAssignmentContainer");
        resourcesCostCategoryAssignmentController = (ResourcesCostCategoryAssignmentController)
            costCategoryAssignmentContainer.getVariable("assignmentController", true);
    }

    public void goToCreateForm() {
        machineModel.initCreate();
        criterionsController.prepareForCreate(machineModel.getMachine());
        configurationController.initConfigurationController(machineModel);
        resourcesCostCategoryAssignmentController.setResource(machineModel.getMachine());
        selectMachineDataTab();
        showEditWindow(_("Create machine"));
        resourceCalendarModel.cancel();
    }

    private void showEditWindow(String title) {
        editWindow.setTitle(title);
        showEditWindow();
    }

    private void showEditWindow() {
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    /**
     * Loads {@link Machine} into model, shares loaded {@link Machine} with
     * {@link CriterionsController}
     *
     * @param machine
     */
    public void goToEditForm(Machine machine) {
        machineModel.initEdit(machine);
        prepareCriterionsForEdit();
        prepareCalendarForEdit();
        selectMachineDataTab();
        showEditWindow(_("Edit machine"));
        configurationController.initConfigurationController(machineModel);
        resourcesCostCategoryAssignmentController.setResource(machineModel.getMachine());
    }

    private void selectMachineDataTab() {
        Tab tabMachineData = (Tab) editWindow.getFellow("tbMachineData");
        tabMachineData.setSelected(true);
    }

    private void prepareCriterionsForEdit() {
        criterionsController.prepareForEdit(machineModel.getMachine());
    }

    private void prepareCalendarForEdit() {
        if (isCalendarNull()) {
            return;
        }

        updateCalendarController();
        resourceCalendarModel.initEdit(machineModel.getCalendarOfMachine());
        try {
            baseCalendarEditionController.doAfterCompose(editCalendarWindow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        baseCalendarEditionController.setSelectedDay(new Date());
        Util.reloadBindings(editCalendarWindow);
        Util.reloadBindings(createNewVersionWindow);
    }

    public void save() {
        validateConstraints();
        try {
            saveCalendar();
            if (!confirmCriterions()) {
                return;
            }
            machineModel.confirmSave();
            goToList();
            messagesForUser.showMessage(Level.INFO, _("Machine saved"));
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    public void saveAndContinue() {
        validateConstraints();
        try {
            saveCalendar();
            if (!confirmCriterions()) {
                return;
            }
            machineModel.confirmSave();
            goToEditForm(machineModel.getMachine());
            messagesForUser.showMessage(Level.INFO,_("Machine saved"));
        } catch (ValidationException e) {
            messagesForUser.showMessage(Level.ERROR,
                    _("Could not save machine") + " " + showInvalidValues(e));
        }
    }

    private void validateConstraints() {
        Tab tab = (Tab) editWindow.getFellowIfAny("tbMachineData");
        try {
            validateMachineDataTab();
            tab = (Tab) editWindow.getFellowIfAny("assignedCriteriaTab");
            criterionsController.validateConstraints();
            tab = (Tab) editWindow.getFellowIfAny("costCategoryAssignmentTab");
            resourcesCostCategoryAssignmentController.validateConstraints();
            // TODO: check 'calendar' tab
        } catch (WrongValueException e) {
            tab.setSelected(true);
            throw e;
        }
    }

    private void validateMachineDataTab() {
        ConstraintChecker.isValid(editWindow
                .getFellowIfAny("machineDataTabpanel"));
    }

    private String showInvalidValues(ValidationException e) {
        String result = "";
        for (InvalidValue each : e.getInvalidValues())
            result = result + each.getMessage();
        return result;
    }

    private void saveCalendar() throws ValidationException {
        if (baseCalendarEditionController != null) {
            baseCalendarEditionController.save();
        }
        if (machineModel.getCalendar() == null) {
            createCalendar();
        }
    }

    private boolean confirmCriterions() throws ValidationException {
        if (criterionsController != null) {
            if (!criterionsController.validate()) {
                return false;
            }
            criterionsController.save();
        }
        return true;
    }

    private void goToList() {
        getVisibility().showOnly(listWindow);
        Util.reloadBindings(listWindow);
    }

    public void cancel() {
        goToList();
    }

    public List<BaseCalendar> getBaseCalendars() {
        return machineModel.getBaseCalendars();
    }

    private IBaseCalendarModel resourceCalendarModel;

    private void createCalendar() {
        Combobox combobox = (Combobox) editWindow
                .getFellow("createDerivedCalendar");
        Comboitem selectedItem = combobox.getSelectedItem();
        if (selectedItem == null) {
            throw new WrongValueException(combobox,
                    _("Please, select a calendar"));
        }

        BaseCalendar parentCalendar = (BaseCalendar) combobox.getSelectedItem()
                .getValue();
        if (parentCalendar == null) {
            parentCalendar = machineModel.getDefaultCalendar();
        }

        machineModel.setCalendar(parentCalendar.newDerivedResourceCalendar());
    }

    private Window editCalendarWindow;

    private Window createNewVersionWindow;

    private BaseCalendarEditionController baseCalendarEditionController;

    private void updateCalendarController() {
        editCalendarWindow = (Window) editWindow
                .getFellowIfAny("editCalendarWindow");
        createNewVersionWindow = (Window) editWindow
                .getFellowIfAny("createNewVersion");

        createNewVersionWindow.setVisible(true);
        createNewVersionWindow.setVisible(false);

        baseCalendarEditionController = new BaseCalendarEditionController(
                resourceCalendarModel, editCalendarWindow,
                createNewVersionWindow) {

            @Override
            public void goToList() {
                machineModel
                        .setCalendarOfMachine((ResourceCalendar) resourceCalendarModel
                                .getBaseCalendar());
                reloadWindow();
            }

            @Override
            public void cancel() {
                resourceCalendarModel.cancel();
                machineModel.setCalendarOfMachine(null);
                reloadWindow();
            }

            @Override
            public void save() {
                ResourceCalendar calendar = (ResourceCalendar) resourceCalendarModel
                        .getBaseCalendar();
                if (calendar != null) {
                    machineModel.setCalendarOfMachine(calendar);
                }
                reloadWindow();
            }

        };

        editCalendarWindow.setVariable("calendarController", this, true);
        createNewVersionWindow.setVariable("calendarController", this, true);
    }

    private void reloadWindow() {
        Util.reloadBindings(editWindow);
    }

    public boolean isCalendarNull() {
        return (machineModel.getCalendarOfMachine() == null);
    }

    public boolean isCalendarNotNull() {
        return !isCalendarNull();
    }

    public BaseCalendarEditionController getEditionController() {
        return baseCalendarEditionController;
    }

    @SuppressWarnings("unused")
    private CriterionsController getCriterionsController() {
        return (CriterionsController) editWindow.getFellow(
                "criterionsContainer").getAttribute(
                "assignedCriterionsController");
    }

    public MachineConfigurationController getConfigurationController() {
        return configurationController;
    }

    public BaseCalendarsComboitemRenderer getBaseCalendarsComboitemRenderer() {
        return baseCalendarsComboitemRenderer;
    }

    private class BaseCalendarsComboitemRenderer implements ComboitemRenderer {

        @Override
        public void render(Comboitem item, Object data) throws Exception {
            BaseCalendar calendar = (BaseCalendar) data;
            item.setLabel(calendar.getName());
            item.setValue(calendar);

            if (isDefaultCalendar(calendar)) {
                Combobox combobox = (Combobox) item.getParent();
                combobox.setSelectedItem(item);
            }
        }

        private boolean isDefaultCalendar(BaseCalendar calendar) {
            BaseCalendar defaultCalendar = machineModel.getDefaultCalendar();
            return defaultCalendar.getId().equals(calendar.getId());
        }

    }

    /**
     * Operations to filter the machines by multiple filters
     */

    public Constraint checkConstraintFinishDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date finishDate = (Date) value;
                if ((finishDate != null)
                        && (filterStartDate.getValue() != null)
                        && (finishDate.compareTo(filterStartDate.getValue()) < 0)) {
                    filterFinishDate.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be greater than start date"));
                }
            }
        };
    }

    public Constraint checkConstraintStartDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date startDate = (Date) value;
                if ((startDate != null)
                        && (filterFinishDate.getValue() != null)
                        && (startDate.compareTo(filterFinishDate.getValue()) > 0)) {
                    filterStartDate.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be lower than finish date"));
                }
            }
        };
    }

    public void onApplyFilter() {
        ResourcePredicate predicate = createPredicate();
        if (predicate != null) {
            filterByPredicate(predicate);
        } else {
            showAllMachines();
        }
    }

    private ResourcePredicate createPredicate() {
        List<FilterPair> listFilters = (List<FilterPair>) bdFilters
                .getSelectedElements();

        String personalFilter = txtfilter.getValue();
        // Get the dates filter
        LocalDate startDate = null;
        LocalDate finishDate = null;
        if (filterStartDate.getValue() != null) {
            startDate = LocalDate.fromDateFields(filterStartDate
                .getValue());
        }
        if (filterFinishDate.getValue() != null) {
            finishDate = LocalDate.fromDateFields(filterFinishDate
                .getValue());
        }

        final Comboitem item = filterLimitedResource.getSelectedItem();
        Boolean isLimitedResource = (item != null) ? LimitedResourceEnum
                .valueOf((LimitedResourceEnum) item.getValue()) : null;

        if (listFilters.isEmpty()
                && (personalFilter == null || personalFilter.isEmpty())
                && startDate == null && finishDate == null
                && isLimitedResource == null) {
            return null;
        }
        return new ResourcePredicate(listFilters, personalFilter, startDate,
                finishDate, isLimitedResource);
    }

    private void filterByPredicate(ResourcePredicate predicate) {
        List<Machine> filteredResources = machineModel
                .getFilteredMachines(predicate);
        listing.setModel(new SimpleListModel(filteredResources.toArray()));
        listing.invalidate();
    }

    private void clearFilterDates() {
        filterStartDate.setValue(null);
        filterFinishDate.setValue(null);
    }

    public void showAllMachines() {
        listing.setModel(new SimpleListModel(machineModel.getAllMachines()
                .toArray()));
        listing.invalidate();
    }

    public Set<LimitedResourceEnum> getLimitedResourceFilterOptionList() {
        return LimitedResourceEnum.getLimitedResourceFilterOptionList();
    }

    public Set<LimitedResourceEnum> getLimitedResourceOptionList() {
        return LimitedResourceEnum.getLimitedResourceOptionList();
    }

    public Object getLimitedResource() {
        final Machine machine = getMachine();
        return (machine != null) ? LimitedResourceEnum.valueOf(machine
                .isLimitedResource())
                : LimitedResourceEnum.NON_LIMITED_RESOURCE;         // Default option
    }

    public void setLimitedResource(LimitedResourceEnum option) {
        Machine machine = getMachine();
        if (machine != null) {
            machine.setLimitedResource(LimitedResourceEnum.LIMITED_RESOURCE.equals(option));
        }
    }

}
