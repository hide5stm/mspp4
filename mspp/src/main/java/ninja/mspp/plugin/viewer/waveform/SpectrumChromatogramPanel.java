/**
 * BSD 3-Clause License
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * @author Mass++ Users Group
 * @author Satoshi Tanaka
 * @since 2018-06-01 05:51:10+09:00
 *
 * Copyright (c) 2018, Mass++ Users Group
 * All rights reserved.
 */
package ninja.mspp.plugin.viewer.waveform;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import ninja.mspp.MsppManager;
import ninja.mspp.annotation.method.ChromatogramPanel;
import ninja.mspp.annotation.method.SpectrumPanel;
import ninja.mspp.model.PluginMethod;

@Component
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class SpectrumChromatogramPanel implements Initializable {
	@FXML
	private TabPane spectrumTabPane;

	@FXML
	private TabPane chromatogramTabPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		MsppManager manager = MsppManager.getInstance();

		List< PluginMethod< SpectrumPanel > > spectrumMethods = manager.getMethods( SpectrumPanel.class );
		for( PluginMethod< SpectrumPanel > spectrumMethod : spectrumMethods ) {
			Node node = ( Node )spectrumMethod.invoke();
			if( node != null ) {
				Tab tab = new Tab( spectrumMethod.getAnnotation().value() );
				BorderPane pane = new BorderPane();
				pane.setCenter( node );
				tab.setContent( pane );
				this.spectrumTabPane.getTabs().add( tab );
			}
		}

		List< PluginMethod< ChromatogramPanel > > chromatogramMethods = manager.getMethods( ChromatogramPanel.class );
		for( PluginMethod< ChromatogramPanel > chromatogramMethod : chromatogramMethods ) {
			Node node = ( Node )chromatogramMethod.invoke();
			if( node != null ) {
				Tab tab = new Tab( chromatogramMethod.getAnnotation().value() );
				BorderPane pane = new BorderPane();
				pane.setCenter( node );
				tab.setContent( pane );
				this.chromatogramTabPane.getTabs().add( tab );
			}
		}
	}

}
