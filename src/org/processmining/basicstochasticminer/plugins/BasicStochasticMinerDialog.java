package org.processmining.basicstochasticminer.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.deckfour.xes.model.XLog;
import org.processmining.basicstochasticminer.BasicStochasticMinerParameters;
import org.processmining.basicstochasticminer.BasicStochasticMinerParametersAbstract;
import org.processmining.basicstochasticminer.BasicStochasticMinerParametersDefault;
import org.processmining.plugins.InductiveMiner.ClassifierChooser;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class BasicStochasticMinerDialog extends JPanel {

	private static final long serialVersionUID = -2812697072111231107L;

	private BasicStochasticMinerParametersAbstract parameters = new BasicStochasticMinerParametersDefault();

	public BasicStochasticMinerDialog(XLog log) {
		SlickerFactory factory = SlickerFactory.instance();

		int leftColumnWidth = 200;
		int columnMargin = 20;
		int rowHeight = 40;

		//setLayout(new GridBagLayout());
		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		//classifiers
		final ClassifierChooser classifiers;
		{
			JLabel classifierLabel = factory.createLabel("Event classifier");
			add(classifierLabel);

			layout.putConstraint(SpringLayout.NORTH, classifierLabel, 5, SpringLayout.NORTH, this);
			layout.putConstraint(SpringLayout.EAST, classifierLabel, leftColumnWidth, SpringLayout.WEST, this);

			classifiers = new ClassifierChooser(log);
			add(classifiers);
			layout.putConstraint(SpringLayout.WEST, classifiers, columnMargin, SpringLayout.EAST, classifierLabel);
			layout.putConstraint(SpringLayout.VERTICAL_CENTER, classifiers, 0, SpringLayout.VERTICAL_CENTER,
					classifierLabel);
		}

		//assumption
		final JLabel assumptionValue;
		{
			JLabel assumptionLabel = factory.createLabel("Assumption");
			add(assumptionLabel);

			layout.putConstraint(SpringLayout.VERTICAL_CENTER, assumptionLabel, rowHeight, SpringLayout.VERTICAL_CENTER,
					classifiers);
			layout.putConstraint(SpringLayout.EAST, assumptionLabel, leftColumnWidth, SpringLayout.WEST, this);

			{
				assumptionValue = factory.createLabel(
						"<html>This plug-in ignores the final markings of the accepting Petri net<br>and instead assumes that every deadlock is a final marking, <br>and that a deadlock is always reachable.</html>");
				add(assumptionValue);

				layout.putConstraint(SpringLayout.WEST, assumptionValue, columnMargin, SpringLayout.EAST,
						assumptionLabel);
				layout.putConstraint(SpringLayout.VERTICAL_CENTER, assumptionValue, 0, SpringLayout.VERTICAL_CENTER,
						assumptionLabel);
			}
		}

		//doi
		final JLabel doiLabel;
		final JLabel doiValue;
		{
			doiLabel = factory.createLabel("More information");
			doiLabel.setVisible(getDoi() != null);
			add(doiLabel);
			layout.putConstraint(SpringLayout.VERTICAL_CENTER, doiLabel, rowHeight, SpringLayout.VERTICAL_CENTER,
					assumptionValue);
			layout.putConstraint(SpringLayout.EAST, doiLabel, leftColumnWidth, SpringLayout.WEST, this);

			doiValue = factory.createLabel("  " + Objects.toString(getDoi(), ""));
			add(doiValue);
			layout.putConstraint(SpringLayout.WEST, doiValue, columnMargin, SpringLayout.EAST, doiLabel);
			layout.putConstraint(SpringLayout.VERTICAL_CENTER, doiValue, 0, SpringLayout.VERTICAL_CENTER, doiLabel);
		}

		classifiers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parameters.setClassifier(classifiers.getSelectedClassifier());
			}
		});

		doiValue.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				String doi = getDoi();
				if (doi != null) {
					IMMiningDialog.openWebPage(doi);
				}
			}
		});
	}

	public String getDoi() {
		return null;
	}

	public BasicStochasticMinerParameters getMiningParameters() {
		return parameters;
	}

}