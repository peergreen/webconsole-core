/*
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.core.notifier.utils;

import com.vaadin.ui.ProgressIndicator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Mohammed Boukada
 */
public class Task {
    private String message;
    private Object worker;
    private Long contentLength;
    private List<ProgressIndicator> progressIndicators = new CopyOnWriteArrayList<>();

    public Task(Object worker, String message, Long contentLength) {
        this.worker = worker;
        this.message = message;
        this.contentLength = contentLength;
    }

    public String getMessage() {
        return message;
    }

    public Object getWorker() {
        return worker;
    }

    public void updateTask(Long bytesReceived) {
        for (ProgressIndicator progressIndicator : progressIndicators) {
            progressIndicator.setValue((float) (bytesReceived / contentLength));
        }
    }

    public void addProgressIndicator(ProgressIndicator progressIndicator) {
        progressIndicators.add(progressIndicator);
    }
}
