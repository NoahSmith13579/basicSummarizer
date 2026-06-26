import SiteLayout from "../layout/SiteLayout.tsx";
import Sidebar from "../layout/Sidebar/Sidebar.tsx";
import InputPanel from "../layout/InputPanel/InputPanel.tsx"
import MiddlePanel from "../layout/MiddlePanel/MiddlePanel.tsx"

export default  function SummariesPage(){
    return (
        <SiteLayout
            left={<Sidebar/>}
            center={<MiddlePanel/>}
            right={<InputPanel/>}
        />
    );
}
