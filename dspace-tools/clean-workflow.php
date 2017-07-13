<?php
$options = getopt("u:p:h", array("user:", "pass:", "help"));

if (isset($options["h"]) || isset($options["help"])) {
    echo <<<EOT
Usage: php ./clean-workflow.php [OPTION] [url]
Remove all items in a DSpace workflow.
Example: php ./clean-workflow.php -u user -p pass https://localhost/rest/

  -h, --help          Display this help and exit.
  -u, --user          The username of a user who can accept/reject/approve
                      DSpace items.
  -p, --pass          The password of a user who can accept/reject/approve
                      DSpace items.
EOT;
    exit();
}

$user = null;
if (isset($options["u"])) {
    $user = $options["u"];
} else if (isset($options["user"])) {
    $user = $options["user"];
}

$pass = null;
if (isset($options["p"])) {
    $pass = $options["p"];
} else if (isset($options["pass"])) {
    $pass = $options["pass"];
}

array_shift($argv);

foreach ($argv as $arg) {
    if ($arg == '-u' || $arg == '-p') {
        array_shift($argv);
        array_shift($argv);
    }

    if (strpos($arg, '--user=') === 0 || strpos($arg, '--pass=') === 0) {
        array_shift($argv);
    }
}

if (!$user || !$pass) {
    die("ERROR: user/password not specified.\n");
}

$url = array_pop($argv);

if (!$url) {
    die("REST url missing.\n");
}

$listWorkflowsUrl = $url."/workflows.json";

$curl = curl_init($listWorkflowsUrl);

$headers = array();
$headers[] = "user: ".$user;
$headers[] = "pass: ".$pass;
$headers[] = "Content-Type: application/json";

curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);

$response = curl_exec($curl);

if ($response === false) {
    $info = curl_getinfo($curl);
    curl_close($curl);
    die('ERROR: ' . var_export($info));
}

$httpCode = curl_getinfo($curl, CURLINFO_HTTP_CODE);

curl_close($curl);

if ((int)$httpCode !== 200) {
    die("ERROR: ".$httpCode." fetching workflow list\n");
}

$result = json_decode($response);
$workflows = $result->workflows;

$data = "";

foreach ($workflows as $workflow) {
    if ((int)$workflow->state !== 4) {
        $acceptWorkflowUrl = $url."/workflows/".$workflow->id."/accept.json";

        $curl = curl_init($acceptWorkflowUrl);

        curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "PUT");
        curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($curl, CURLOPT_FAILONERROR, true);
        curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
        curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);

        $response = curl_exec($curl);

        if ($response === false) {
            $info = curl_getinfo($curl);
            curl_close($curl);
            echo 'ERROR: ' . var_export($info);
        }

        $httpCode = curl_getinfo($curl, CURLINFO_HTTP_CODE);

        curl_close($curl);

        if ((int)$httpCode !== 204) {
            echo "ERROR: ".$httpCode." accepting workflow item\n";
        }

        echo "workflow ".$workflow->id." accepted\n";

        $approveWorkflowUrl = $url."/workflows/".$workflow->id."/approve.json";

        $curl = curl_init($approveWorkflowUrl);

        curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "PUT");
        curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($curl, CURLOPT_FAILONERROR, true);
        curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
        curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);

        $response = curl_exec($curl);

        if ($response === false) {
            $info = curl_getinfo($curl);
            curl_close($curl);
            echo 'ERROR: ' . var_export($info);
        }

        $httpCode = curl_getinfo($curl, CURLINFO_HTTP_CODE);

        if ((int)$httpCode !== 204) {
            echo "ERROR: ".$httpCode." approving workflow item\n";
        }

        $info = curl_getinfo($curl);

        curl_close($curl);

        echo "workflow ".$workflow->id." approved. Item id " . $workflow->item->id."\n";

        $deleteItemUrl = $url."/items/".$workflow->item->id.".json";

        $curl = curl_init($deleteItemUrl);

        curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "DELETE");
        curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($curl, CURLOPT_FAILONERROR, true);
        curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
        curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);

        $response = curl_exec($curl);

        $info = curl_getinfo($curl);

        if ($response === false) {
            curl_close($curl);
            echo 'ERROR: ' . var_export($info);
        }

        $httpCode = curl_getinfo($curl, CURLINFO_HTTP_CODE);

        if ((int)$httpCode !== 204) {
            echo "ERROR: ".$httpCode." deleting archived item\n";
        }

        echo "Item ".$workflow->item->id." deleted\n";

        curl_close($curl);
    }
}
