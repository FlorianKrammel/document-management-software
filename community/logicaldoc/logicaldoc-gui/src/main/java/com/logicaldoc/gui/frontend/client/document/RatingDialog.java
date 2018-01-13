package com.logicaldoc.gui.frontend.client.document;

import java.util.LinkedHashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.logicaldoc.gui.common.client.Session;
import com.logicaldoc.gui.common.client.beans.GUIDocument;
import com.logicaldoc.gui.common.client.beans.GUIRating;
import com.logicaldoc.gui.common.client.data.RatingsDS;
import com.logicaldoc.gui.common.client.formatters.DateCellFormatter;
import com.logicaldoc.gui.common.client.i18n.I18N;
import com.logicaldoc.gui.common.client.log.Log;
import com.logicaldoc.gui.common.client.observer.DocumentController;
import com.logicaldoc.gui.common.client.util.ItemFactory;
import com.logicaldoc.gui.common.client.util.Util;
import com.logicaldoc.gui.frontend.client.services.DocumentService;
import com.smartgwt.client.core.Rectangle;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.HeaderControls;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.TitleOrientation;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.DoubleClickEvent;
import com.smartgwt.client.widgets.events.DoubleClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.ValuesManager;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.FormItemIcon;
import com.smartgwt.client.widgets.form.fields.PickerIcon;
import com.smartgwt.client.widgets.form.fields.PickerIcon.Picker;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemClickHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemIconClickEvent;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.layout.VLayout;

public class RatingDialog extends Window {
	private int docRating;

	private GUIRating rating = null;

	private ValuesManager vm = new ValuesManager();

	public RatingDialog(int documentRating, GUIRating rat) {
		super();
		this.docRating = documentRating;
		this.rating = rat;

		addCloseClickHandler(new CloseClickHandler() {
			@Override
			public void onCloseClick(CloseClickEvent event) {
				destroy();
			}
		});

		setHeaderControls(HeaderControls.HEADER_LABEL, HeaderControls.CLOSE_BUTTON);
		setTitle(I18N.message("rating"));
		setWidth(250);
		setHeight(150);
		setCanDragResize(true);
		setIsModal(true);
		setShowModalMask(true);
		centerInPage();
		setPadding(5);
		setAutoSize(true);
		setAlign(Alignment.LEFT);

		final VLayout layout = new VLayout(5);
		layout.setTop(20);
		layout.setMargin(5);

		final DynamicForm ratingForm = new DynamicForm();
		ratingForm.setAlign(Alignment.LEFT);
		ratingForm.setTitleOrientation(TitleOrientation.LEFT);
		ratingForm.setNumCols(1);
		ratingForm.setValuesManager(vm);

		FormItemIcon ratingIcon = ItemFactory.newItemIcon("rating" + this.docRating + ".png");
		StaticTextItem actualRating = ItemFactory.newStaticTextItem("actualrating", "actualrating", "");
		actualRating.setIcons(ratingIcon);
		actualRating.setEndRow(true);
		actualRating.setIconWidth(88);

		final StaticTextItem totalVotes = ItemFactory.newStaticTextItem("totalvotes", "totalvotes", this.rating
				.getCount().toString());
		totalVotes.setWrapTitle(false);
		totalVotes.setWrap(false);
		totalVotes.setEndRow(true);
		totalVotes.setAlign(Alignment.LEFT);

		final PickerIcon searchPicker = new PickerIcon(new Picker("[SKIN]/actions/search.png"),
				new FormItemClickHandler() {
					public void onFormItemClick(FormItemIconClickEvent event) {

						ListGridField vote = new ListGridField("vote", I18N.message("vote"), 94);
						vote.setAlign(Alignment.CENTER);
						vote.setType(ListGridFieldType.IMAGE);
						vote.setImageURLPrefix(Util.imagePrefix() + "/rating");
						vote.setImageURLSuffix(".png");
						vote.setImageWidth(90);

						ListGridField user = new ListGridField("user", I18N.message("user"), 140);
						ListGridField date = new ListGridField("date", I18N.message("date"));
						date.setAlign(Alignment.CENTER);
						date.setCellFormatter(new DateCellFormatter(true));

						ListGrid votesGrid = new ListGrid();
						votesGrid.setWidth100();
						votesGrid.setHeight100();
						votesGrid.setAutoFetchData(true);
						votesGrid.setDataSource(new RatingsDS(rating.getDocId()));
						votesGrid.setFields(date, user, vote);

						final Window dialog = new Window();
						dialog.setAutoCenter(true);
						dialog.setIsModal(true);
						dialog.setShowHeader(false);
						dialog.setShowEdges(false);
						dialog.setWidth(340);
						dialog.setHeight(200);
						dialog.setCanDragResize(true);
						dialog.setDismissOnEscape(true);
						dialog.addItem(votesGrid);
						dialog.addDoubleClickHandler(new DoubleClickHandler() {

							@Override
							public void onDoubleClick(DoubleClickEvent event) {
								dialog.destroy();
							}
						});

						dialog.show();

						// get global coordinates of the clicked picker icon
						Rectangle iconRect = totalVotes.getIconPageRect(event.getIcon());
						dialog.moveTo(iconRect.getLeft(), iconRect.getTop());

					}
				});
		searchPicker.setPrompt(I18N.message("showvoters"));
		totalVotes.setIcons(searchPicker);
		totalVotes.setIconHSpace(2);

		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		for (int i = 1; i <= 5; i++)
			map.put("" + i, "" + i);

		final SelectItem yourVote = new SelectItem("stars", I18N.message("yourvote"));
		yourVote.setWrapTitle(false);
		yourVote.setWidth(120);
		yourVote.setValueMap(map);
		yourVote.setPickListWidth(90);

		LinkedHashMap<String, String> valueIcons = new LinkedHashMap<String, String>();
		for (int i = 1; i <= 5; i++)
			valueIcons.put("" + i, "rating" + i);
		yourVote.setImageURLPrefix(Util.imagePrefix());
		yourVote.setImageURLSuffix(".png");
		yourVote.setValueIcons(valueIcons);
		yourVote.setValueIconWidth(80);

		ButtonItem vote = new ButtonItem();
		vote.setTitle(I18N.message("vote"));
		vote.setAutoFit(true);
		vote.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				vm.validate();
				if (!vm.hasErrors()) {
					RatingDialog.this.rating.setUserId(Session.get().getUser().getId());
					RatingDialog.this.rating.setVote(Integer.parseInt(vm.getValueAsString("stars")));

					DocumentService.Instance.get().saveRating(RatingDialog.this.rating, new AsyncCallback<Integer>() {

						@Override
						public void onFailure(Throwable caught) {
							Log.serverError(caught);
							destroy();
						}

						@Override
						public void onSuccess(Integer rating) {
							Log.info(I18N.message("votesaved"), null);
							afterSaveOrDelete(rating);
						}
					});
				}
				destroy();
			}
		});

		ratingForm.setItems(actualRating, totalVotes, yourVote, vote);
		layout.addMember(ratingForm);

		DocumentService.Instance.get().getUserRating(rat.getDocId(), new AsyncCallback<GUIRating>() {

			@Override
			public void onFailure(Throwable caught) {
				Log.serverError(caught);
			}

			@Override
			public void onSuccess(final GUIRating vote) {
				if (vote != null) {
					yourVote.setValue("" + vote.getVote());

					final DynamicForm alreadyVotedForm = new DynamicForm();
					alreadyVotedForm.setAlign(Alignment.LEFT);
					alreadyVotedForm.setTitleOrientation(TitleOrientation.TOP);
					alreadyVotedForm.setNumCols(1);

					StaticTextItem alreadyVoted = ItemFactory.newStaticTextItem("alreadyVoted", "",
							"<b>" + I18N.message("alreadyvoted") + "</b>");
					alreadyVoted.setShouldSaveValue(false);
					alreadyVoted.setAlign(Alignment.LEFT);
					alreadyVoted.setTextBoxStyle("footerWarn");
					alreadyVoted.setShowTitle(false);
					alreadyVoted.setWrapTitle(false);
					alreadyVoted.setWrap(false);
					alreadyVotedForm.setItems(alreadyVoted);

					ButtonItem delete = new ButtonItem("delete", I18N.message("deleteyourvote"));
					delete.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							DocumentService.Instance.get().deleteRating(vote.getId(), new AsyncCallback<Integer>() {

								@Override
								public void onFailure(Throwable caught) {
									Log.serverError(caught);
								}

								@Override
								public void onSuccess(Integer rating) {
									afterSaveOrDelete(rating);
								}
							});
						}
					});

					alreadyVotedForm.setItems(alreadyVoted, delete);
					layout.addMember(alreadyVotedForm);
				}

			}
		});

		addItem(layout);
	}

	private void afterSaveOrDelete(Integer rat) {
		// We have to reload the document because
		// the rating is changed. We need to know if
		// this operation into the Documents list
		// panel or into the Search list panel.
		DocumentService.Instance.get().getById(rating.getDocId(), new AsyncCallback<GUIDocument>() {

			@Override
			public void onFailure(Throwable caught) {
				Log.serverError(caught);
			}

			@Override
			public void onSuccess(GUIDocument doc) {
				DocumentController.get().modified(doc);
				destroy();
			}
		});
	}
}