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
 * Task descriptor
 * @author Mohammed Boukada
 */
public class Task {
    /**
     * Task message
     */
    private String message;

    /**
     * Task worker
     */
    private Object worker;

    /**
     * Task length
     */
    private Long contentLength;

    /**
     * Task progress indicator
     */
    private List<ProgressIndicator> progressIndicators = new CopyOnWriteArrayList<>();

    /**
     * Create a task descriptor
     * @param worker task worker
     * @param message task message
     * @param contentLength task length
     */
    public Task(Object worker, String message, Long contentLength) {
        this.worker = worker;
        this.message = message;
        this.contentLength = contentLength;
    }

    /**
     * Get task message
     * @return task message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get task worker
     * @return task worker
     */
    public Object getWorker() {
        return worker;
    }

    /**
     * Update task descriptor
     * @param bytesReceived bytes received
     */
    public void updateTask(Long bytesReceived) {
        for (ProgressIndicator progressIndicator : progressIndicators) {
            progressIndicator.setValue((float) (bytesReceived / contentLength));
        }
    }

    /**
     * Add task progress indicator
     * @param progressIndicator progress indicator
     */
    public void addProgressIndicator(ProgressIndicator progressIndicator) {
        progressIndicators.add(progressIndicator);
    }
}
